package com.poeny.keywords_filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.poeny.keywords_filter.manager.ConnectionManager;
import com.poeny.keywords_filter.tool.Sets;

public class Filter {

	private static Filter filter;

	private static final Logger LOGGER = LoggerFactory.getLogger(Filter.class);

	public static Filter getFilter() {
		if (filter == null) {
			filter = new Filter();
		}
		return filter;
	}

	/**
	 * 分词器，将黑龙江省分成黑龙， 龙江， 江省
	 * 用于建立索引
	 * @param word
	 * @return
	 */
	private List<String> splitWord(String word) {
		List<String> ans = new ArrayList<String>();
		if (word.length() <= 1) {
			return ans;
		}
		for (int i = 2; i <= word.length(); i++) {
			ans.add(word.substring(i - 2, i));
		}
		return ans;
	}

	/**
	 * 具体更新索引map的操作
	 * 
	 * @param subWord
	 * @param word
	 * @param map
	 */
	private void updateMap(String subWord, String word, HashMap<String, Set<String>> map) {
		Set<String> set = map.get(subWord);
		if (set == null) {
			set = new HashSet<String>();
			set.add(word);
			map.put(subWord, set);
		} else {
			set.add(word);
		}
	}

	/**
	 * 建立索引 如黑龙江 则建立索引 黑龙 -> 黑龙江, 龙江 -> 黑龙江
	 * 
	 * @param word
	 * @author BAO
	 */
	private void makeIndex(String word, HashMap<String, Set<String>> map) {
		List<String> subWord = splitWord(word);
		for (String w : subWord) {
			updateMap(w, word, map);
		}
	}

	/**
	 * 获取一个应该被过滤的词汇的set
	 * 
	 * @param set
	 * @param cache
	 * @return
	 */
	private Set<String> getFilterWords(Set<String> set, HashSet<String> cache) {
		List<String> list = new ArrayList<String>();
		Set<String> subWord = new HashSet<String>();
		for (String word : set) {
			if (!cache.contains(word)) {
				list.add(word);
			}
		}
		Collections.sort(list, new Comparator<String>() {
			public int compare(String a, String b) {
				if (a.length() > b.length())
					return 1;
				if (a.length() == b.length())
					return 0;
				return -1;
			}
		});

		for (int i = 0; i < list.size(); i++) {
			String w = list.get(i);
			if (w.equals("")) {
				continue;
			}
			subWord.add(w);
			for (int j = i + 1; j < list.size(); j++) {
				String word = list.get(j);
				if (word.equals("")) {
					continue;
				}
				list.set(j, word.replaceAll(".*" + Matcher.quoteReplacement(w) + ".*", ""));
			}
		}
		set.removeAll(subWord);
		return set;
	}

	/**
	 * 过滤掉一个set中可能包含的词汇，如黑龙江，黑龙江省，只留下黑龙江
	 * 
	 * @param wordSet
	 * @return
	 */
	public Set<String> filterWords(Set<String> wordSet) {
		HashMap<String, Set<String>> map = new HashMap<String, Set<String>>();
		HashSet<String> cache = new HashSet<String>();

		for (String word : wordSet) {
			makeIndex(word, map);
		}
		for (Set<String> set : map.values()) {
			try {
				Set<String> tmp = getFilterWords(set, cache);

				if (tmp != null) {
					cache.addAll(tmp);
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		wordSet.removeAll(cache);
		map.clear();
		cache.clear();
		return wordSet;
	}

	/**
	 * 返回2个set所有keyword互相过滤之后的结果
	 * 
	 * @param set1
	 * @param set2
	 * @return
	 */
	public Set<String> filterWords(Set<String> set1, Set<String> set2) {
		return filterWords(Sets.union(set1, set2));
	}
}
