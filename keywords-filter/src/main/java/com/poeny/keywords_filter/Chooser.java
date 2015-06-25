package com.poeny.keywords_filter;

import java.util.List;
import java.util.Set;

import com.poeny.keywords_filter.cache.KeywordsFreqCache;
import com.poeny.keywords_filter.model.SubjectKeywords;

/**
 * 为每一个SubjectKeywords选择一组关键词组，选择词频最小的那组 在选择之前，要先更新词频到KeywordsFreqCache
 * 
 * @author BAO
 *
 */
public class Chooser {
	private static KeywordsFreqCache freqCache = KeywordsFreqCache.getCache();

	public static int clacFreq(Set<String> wordSet) {
		int res = 0;
		for (String word : wordSet) {
			res += freqCache.getWordFreq(word);
		}
		return res;
	}

	public static void chooseSet(SubjectKeywords sk) {
		List<Set<String>> keywordsSetList = sk.getKeywordsSetList();
		int size = keywordsSetList.size();
		if (size == 1) {
			sk.setChoosedKeywordSetIndex(0);
		}
		int min = 0xffff;
		int p = -1;
		for (int i = 0; i < size; i++) {
			int freq = clacFreq(keywordsSetList.get(i));
			if (freq < min) {
				min = freq;
				p = i;
			}
		}
		sk.setChoosedKeywordSetIndex(p);
	}

	public static void chooseAll(List<SubjectKeywords> skList) {
		for (SubjectKeywords sk : skList) {
			chooseSet(sk);
		}
	}
}
