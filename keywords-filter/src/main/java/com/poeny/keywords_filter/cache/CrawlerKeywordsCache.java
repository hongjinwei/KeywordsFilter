package com.poeny.keywords_filter.cache;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.poeny.keywords_filter.keywords_accessor.CrawlerKeywordsAccessor;
import com.poeny.keywords_filter.tool.Sets;

/**
 * 爬虫取用的关键字的cache，本cache有2部分，一部分是用于未清洗之前的单词(localWords)，用来与远程做比较
 * 另外一部分是remoteWords，这个是crawler远程SQL表中单词的缓存，使用之前需要初始化，从远程库中取出数据
 * 
 * @author BAO
 *
 */
public class CrawlerKeywordsCache {

	private static CrawlerKeywordsAccessor crawlerKeywords = CrawlerKeywordsAccessor.getInstance();

	private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerKeywordsCache.class);

	private static CrawlerKeywordsCache cache = new CrawlerKeywordsCache();

	/**
	 * 经过选择之后，统计所有关键字及其个数，根据这个来找出是否需要更新到数据库
	 */
	private static HashMap<String, Integer> localWords = new HashMap<String, Integer>();

	/**
	 * crawler关键字数据库的缓存数据
	 */
	private static Set<String> remoteWords = new HashSet<String>();

	/**
	 * 远程crawler关键字中，所有type=10的缓存
	 * 
	 * @return
	 */
	private static Set<String> remoteMyWords = new HashSet<String>();

	public static CrawlerKeywordsCache getCache() {
		return cache;
	}

	public boolean init() {
		try {
			remoteWords = crawlerKeywords.getAllKeywords();
			remoteMyWords = crawlerKeywords.getKeywordsByType(10);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return false;
		}
		return true;
	}


	public void updateRemoteWordsCache(Set<String> inserted, Set<String> deleted) {
		remoteWords.removeAll(deleted);
		remoteWords.addAll(inserted);
		remoteMyWords.removeAll(deleted);
		remoteMyWords.addAll(inserted);
	}

	public Set<String> getAllRemoteWords() {
		return remoteWords;
	}
	
	public Set<String> getMyRemoteWords() {
		return remoteMyWords;
	}

	public Set<String> getNotMyRemoteWords() {
		return Sets.difference(remoteWords, remoteMyWords);
	}
	
	public Set<String> getLocalWords() {
		return localWords.keySet();
	}

	public int getWordFreq(String word) {
		if (!localWords.containsKey(word)) {
			return 1;
		}
		int freq = localWords.get(word);
		return freq;
	}

	public boolean contains(String word) {
		return localWords.containsKey(word);
	}

	public int get(String word) {
		return localWords.get(word);
	}

	public void put(String word, int freq) {
		localWords.put(word, freq);
	}

	public void remove(String word) {
		localWords.remove(word);
	}

	public void add(String word) {
		if (localWords.containsKey(word)) {
			localWords.put(word, localWords.get(word) + 1);
		} else {
			localWords.put(word, 1);
		}
	}

	public void delete(String word) {
		if (localWords.containsKey(word)) {
			int freq = localWords.get(word);
			if (freq <= 1) {
				localWords.remove(word);
			} else {
				localWords.put(word, freq - 1);
			}
		}
	}
	
	public void show() {
		LOGGER.info( "CrawlerKeywordsCache : \n local words :\n " + localWords+ "\n remote words : \n" + remoteMyWords);
	}
}
