package com.poeny.keywords_filter.updater;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.poeny.keywords_filter.Filter;
import com.poeny.keywords_filter.cache.CrawlerKeywordsCache;
import com.poeny.keywords_filter.cache.KeywordsFreqCache;
import com.poeny.keywords_filter.cache.SubjectKeywordsCache;
import com.poeny.keywords_filter.model.SubjectKeywords;
import com.poeny.keywords_filter.model.UpdateCacheResult;
import com.poeny.keywords_filter.tool.Sets;

/**
 * 更新本地的cache
 * 
 * @author BAO
 *
 */
public class CacheUpdater {

	private static CrawlerKeywordsCache crawlerWordsCache = CrawlerKeywordsCache.getCache();

	private static SubjectKeywordsCache subjctCache = SubjectKeywordsCache.getCache();

	private static final Logger LOGGER = LoggerFactory.getLogger(CacheUpdater.class);

	/**
	 * 更新subjectCache和crawlerWordsCache
	 * 
	 * 
	 */
	private static void updateUpatedSubjectKeywords(SubjectKeywords sk) {
		Set<String> choosedSet = sk.getChoosedKeywordSet();
		int subjectId = sk.getSubjectId();
		if (choosedSet == null) {
			LOGGER.error("未选择一个set！ subjectId: " + subjectId);
			return;
		}
		Set<String> cachedSet = subjctCache.getChoosedSet(subjectId);
		if (cachedSet != null) {
			cachedSet.addAll(cachedSet);
		}
		Set<String> newAddedKeywords = Sets.difference(choosedSet, cachedSet);
		Set<String> deletedKeywords = Sets.difference(cachedSet, choosedSet);
		for (String word : newAddedKeywords) {
			if (crawlerWordsCache.contains(word)) {
				crawlerWordsCache.put(word, crawlerWordsCache.get(word) + 1);
			} else {
				crawlerWordsCache.put(word, 1);
			}
		}

		for (String word : deletedKeywords) {
			if (crawlerWordsCache.contains(word)) {
				int freq = crawlerWordsCache.get(word);
				if (freq <= 1) {
					crawlerWordsCache.remove(word);
				} else {
					crawlerWordsCache.put(word, freq - 1);
				}
			}
		}
		subjctCache.put(subjectId, sk);
	}

	/**
	 * 更新并且计算单词的词频，以便选择一组关键字
	 * 
	 * @param updated
	 * @param deleted
	 */
	public static void updateSubjectKeyWordsFreq(List<SubjectKeywords> updated, List<SubjectKeywords> deleted) {
		for (SubjectKeywords sk : updated) {
			List<Set<String>> cachedWords = subjctCache.getAllWords(sk.getSubjectId());
			if (cachedWords != null) {
				for (Set<String> words : cachedWords) {
					for (String word : words) {
						KeywordsFreqCache.getCache().delete(word);
					}
				}
			}

			for (Set<String> words : sk.getKeywordsSetList()) {
				for (String word : words) {
					KeywordsFreqCache.getCache().add(word);
				}
			}
		}

		for (SubjectKeywords sk : deleted) {
			for (Set<String> words : sk.getKeywordsSetList()) {
				for (String word : words) {
					KeywordsFreqCache.getCache().delete(word);
				}
			}
		}

	}

	private static void updateDeletedSubjectKeywords(SubjectKeywords sk) {
		int subjectId = sk.getSubjectId();
		Set<String> deletedSet = null;
		deletedSet = subjctCache.getChoosedSet(subjectId);
		if (deletedSet == null) {
			LOGGER.info("CrawlerSubjectCache中没有该项Subject， subjectId：" + subjectId);
			return;
		}

		for (String word : deletedSet) {
			if (crawlerWordsCache.contains(word)) {
				int freq = crawlerWordsCache.get(word);
				if (freq <= 1) {
					crawlerWordsCache.remove(word);
				} else {
					crawlerWordsCache.put(word, freq - 1);
				}
			}
		}
		subjctCache.remove(subjectId);
	}

	/**
	 * 更新crawlerWordsCache，由subject表读出数据后，进行组内清洗，然后更新词频，根据词频计算出选择的一组词
	 * 然后根据本地缓存的ChoosedSubjectKeywordsCache
	 * ，对比获取删除的词汇或新增的词汇，对这些改动进行计数，更新到crawlerWordsCache的localWords中去
	 * 对于新增的就说明有新词，如果删除
	 * ，计数减一，如果到0说明该词已经不需要，则删除该词，最后根据localWords中所有存在的单词，得到所有的词，对比远程所有关键词
	 * ，得到需要插入或者删除的数据 需要插入的数据 = 本地词汇中有的而远程词汇中没有的 需要删除的数据 =
	 * 本地词汇没有的而远程type！=10的词汇中有的
	 * 
	 * @param updated
	 * @param deleted
	 * @return
	 */
	public static UpdateCacheResult updateCache(List<SubjectKeywords> updated, List<SubjectKeywords> deleted) {
		UpdateCacheResult ans = new UpdateCacheResult();
		for (SubjectKeywords sk : updated) {
			updateUpatedSubjectKeywords(sk);
		}

		for (SubjectKeywords sk : deleted) {
			updateDeletedSubjectKeywords(sk);
		}

		Set<String> AllRemoteCrawlerKeywords = crawlerWordsCache.getAllRemoteWords();
		Set<String> MyRemoteCrawlerKeywords = crawlerWordsCache.getMyRemoteWords();
		Set<String> localWords = crawlerWordsCache.getLocalWords();
//		Set<String> notMyTypeWords = crawlerWordsCache.getNotMyRemoteWords();

		Set<String> filteredWords = Filter.getFilter().filterWords(localWords, AllRemoteCrawlerKeywords);

		Set<String> shouldDeleted = Sets.difference(MyRemoteCrawlerKeywords, filteredWords);
		Set<String> shouldInserted = Sets.difference(filteredWords, AllRemoteCrawlerKeywords);
		crawlerWordsCache.updateRemoteWordsCache(shouldInserted, shouldDeleted);
		ans.addAllShouldDeleted(shouldDeleted);
		ans.addAllShouldInsert(shouldInserted);
		return ans;
	}
}
