package com.poeny.keywords_filter.model;

import java.util.HashSet;
import java.util.Set;

public class UpdateCacheResult {

	private Set<String> shouldInsert = new HashSet<String>();

	private Set<String> shouldDeleted = new HashSet<String>();

	public Set<String> getShouldInsert() {
		return shouldInsert;
	}

	public void addShouldInsert(String word) {
		this.shouldInsert.add(word);
	}

	public void addAllShouldInsert(Set<String> words) {
		this.shouldInsert.addAll(words);
	}
	
	public void addAllShouldDeleted(Set<String> words) {
		this.shouldDeleted.addAll(words);
	}
	
	public void mergeResult(UpdateCacheResult result) {
		this.shouldInsert.addAll(result.getShouldInsert());
		this.shouldDeleted.addAll(result.getShouldDeleted());
	}
	
	public Set<String> getShouldDeleted() {
		return shouldDeleted;
	}

	public void addShouldDeleted(String word) {
		this.shouldDeleted.add(word);
	}
	
}
