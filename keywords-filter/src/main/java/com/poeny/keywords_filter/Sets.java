package com.poeny.keywords_filter;

/**
 * set 操作
 * @author BAO
 */

import java.util.*;
public class Sets {

	/**
	 * 返回一个合并后的set
	 */
	public static <T> Set<T> union(Set<T> a, Set<T> b) {
		Set<T> result = new HashSet<T>(a);
		result.addAll(b);
		return result;
	}

	/**
	 * 返回一个交集的set
	 */
	public static <T> Set<T> intersection(Set<T> a, Set<T> b) {
		Set<T> result = new HashSet<T>(a);
		result.retainAll(b);
		return result;
	}
	
	/**
	 * 返回一个除subSet中元素之外所有的superSet元素的set
	 */
	public static <T> Set<T> difference(Set<T> superSet, Set<T> subSet) {
		Set<T> result = new HashSet<T>(superSet);
		result.removeAll(subSet);
		return result;
	}
	
	/**
	 * 返回2个set中除了交集之外的所有元素所组成的set
	 */
	public static <T> Set<T> complement(Set<T> a, Set<T> b) {
		return difference(union(a,b), intersection(a, b));
	}
	
}
