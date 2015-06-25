package com.poeny.keywords_filter.tool;

import java.util.HashSet;
import java.util.Set;

public class WordsUtil {

	public static String cleanWord(String word) {
		if (word == null || word.length() <= 1) {
			return "";
		}
		word = word.replaceAll("[\\(|\\)|\\*|\\.]", "");
		return word.trim();
	}
	
	/**
	 * 从原始subject取出的数据切分出单独的word，并且包装成set后返回
	 * 原始数据为空格分割的多个词汇  例如：市长 副市长 县长 副县长
	 * @param multiWordsStr
	 * @return
	 */
	public static Set<String> getSubjectKeywordSetFromRawstr(String multiWordsStr) {
		Set<String> res = new HashSet<String>();
		String[] keywords = multiWordsStr.split(" ");
		for(int i=0; i< keywords.length; i++) {
			String word = cleanWord(keywords[i]);
			if(!word.equals("")){
				res.add(word);
			}
		}
		return res;
	}
}
