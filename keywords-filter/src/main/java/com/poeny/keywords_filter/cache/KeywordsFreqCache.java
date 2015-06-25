package com.poeny.keywords_filter.cache;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KeywordsFreqCache {

	private static KeywordsFreqCache cache = new KeywordsFreqCache();

	private static HashMap<String, Integer> freqMap = new HashMap<String, Integer>();
	
	private static final Logger LOGGER = LoggerFactory.getLogger(KeywordsFreqCache.class);

	public static KeywordsFreqCache getCache() {
		return cache;
	}

	public int getWordFreq(String word) {
		if (!freqMap.containsKey(word)) {
			return 1;
		}
		int freq = freqMap.get(word);
		return freq;
	}

	public boolean contains(String word) {
		return freqMap.containsKey(word);
	}

	public int get(String word) {
		return freqMap.get(word);
	}

	public void put(String word, int freq) {
		freqMap.put(word, freq);
	}

	public void remove(String word) {
		freqMap.remove(word);
	}

	public void add(String word) {
		if (freqMap.containsKey(word)) {
			freqMap.put(word, freqMap.get(word) + 1);
		} else {
			freqMap.put(word, 1);
		}
	}

	public void delete(String word) {
		if (freqMap.containsKey(word)) {
			int freq = freqMap.get(word);
			if (freq <= 1) {
				freqMap.remove(word);
			} else {
				freqMap.put(word, freq - 1);
			}
		}
	}
	
	public void show() {
		LOGGER.info("KeywordsFreqCache: \n" + freqMap);
	}
}
