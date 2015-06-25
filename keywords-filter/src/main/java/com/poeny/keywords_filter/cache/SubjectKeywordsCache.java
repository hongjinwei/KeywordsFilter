package com.poeny.keywords_filter.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.poeny.keywords_filter.model.SubjectKeywords;

public class SubjectKeywordsCache {

	// private static final Cache subjectCache =
	// CacheManager.create().getCache("subjectKeywords");

	private static final Logger LOGGER = LoggerFactory.getLogger(SubjectKeywordsCache.class);

	private static final HashMap<Integer, SubjectKeywords> subjectCache = new HashMap<Integer, SubjectKeywords>();

	private static SubjectKeywordsCache instance = new SubjectKeywordsCache();

	public static SubjectKeywordsCache getCache() {
		return instance;
	}

	public void put(int subjectId, SubjectKeywords sk) {
		subjectCache.put(subjectId, sk);
	}

	public Set<String> getChoosedSet(int subjectId) {
		SubjectKeywords sk = subjectCache.get(subjectId);
		if (sk == null) {
			return null;
		}
		return sk.getChoosedKeywordSet();
	}

	public SubjectKeywords get(int subjectId) {
		return subjectCache.get(subjectId);
	}

	public List<Set<String>> getAllWords(int subjectId) {
		SubjectKeywords sk = subjectCache.get(subjectId);
		if (sk == null) {
			return null;
		}
		return sk.getKeywordsSetList();
	}

	public void remove(int subjectId) {
		subjectCache.remove(subjectId);
	}

	public void show() {
		LOGGER.info("choosed keywords cache : \n" + subjectCache);
	}
}
