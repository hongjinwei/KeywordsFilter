package com.poeny.keywords_filter.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * subject数据缓存结构, keywordsSetList中所有的关键词都经过crawlerKyewords表中type！=10的词汇的清洗
 * 
 * @author BAO
 *
 */
public class SubjectKeywords {

	/**
	 * 专题id
	 */
	private int subjectId;

	/**
	 * 状态，状态为1表示可用，为0表示删除
	 */
	private int state;

	/**
	 * 最近一次更新的时间
	 */
	private Timestamp updateTime;

	/**
	 * 多组关键词
	 */
	private List<Set<String>> keywordsSetList = new ArrayList<Set<String>>();

	/**
	 * 最终选择刷新到crawler表中的那组关键词是keywordsSetList中的哪一个
	 */
	private int choosedKeywordSetIndex = -1;

	public SubjectKeywords(int subjectId, int state, Timestamp updateTime) {
		super();
		this.subjectId = subjectId;
		this.state = state;
		this.updateTime = updateTime;
	}

	public SubjectKeywords() {
	}

	public int getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(int subjectId) {
		this.subjectId = subjectId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public List<Set<String>> getKeywordsSetList() {
		return keywordsSetList;
	}

	public int getChoosedKeywordSetIndex() {
		return choosedKeywordSetIndex;
	}

	public void setChoosedKeywordSetIndex(int choosedKeywordSetIndex) {
		this.choosedKeywordSetIndex = choosedKeywordSetIndex;
	}

	public Set<String> getChoosedKeywordSet() {
		if (this.choosedKeywordSetIndex != -1) {
			return keywordsSetList.get(this.choosedKeywordSetIndex);
		}
		return null;
	}

	public void addKeywordSet(Set<String> keywords) {
		keywordsSetList.add(keywords);
	}

	public String toString() {
		if (this.choosedKeywordSetIndex != -1) {
			return "{choosedSet: " + getChoosedKeywordSet() + "}";
		} else {
			return "{not choose a set}";
		}
	}
}
