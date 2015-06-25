package com.poeny.keywords_filter.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class QuerySubjectResult {

	private List<SubjectKeywords> updatedSubject = new ArrayList<SubjectKeywords>();

	private List<SubjectKeywords> deletedSubject = new ArrayList<SubjectKeywords>();

	/**
	 * 用来标记已经存在的subject的SubjectKeywords
	 */
	private HashMap<Integer, SubjectKeywords> existedSubjectMap = new HashMap<Integer, SubjectKeywords>();

	public void add(int subjectId, int state, Timestamp updateTime, Set<String> keywords) {
		if(existedSubjectMap.containsKey(subjectId)){
			existedSubjectMap.get(subjectId).addKeywordSet(keywords);
		}else{
			SubjectKeywords sk =  new SubjectKeywords(subjectId, state, updateTime);
			sk.addKeywordSet(keywords);
			existedSubjectMap.put(subjectId, sk);
			if(state == 0) {
				deletedSubject.add(sk);
			}else{
				updatedSubject.add(sk);
			}
		}
	}

	public List<SubjectKeywords> getUpdatedSubjectKeywords() {
		return this.updatedSubject;
	}
	
	public List<SubjectKeywords> getDeletedSubjectKeywords() {
		return this.deletedSubject;
	}
	
	
	public void close() {
		this.deletedSubject.clear();
		this.updatedSubject.clear();
		this.existedSubjectMap.clear();
	}
}
