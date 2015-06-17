package com.poeny.keywords_filter;

import java.security.Timestamp;

public class KeywordsDesign {

	private int id;
	
	private String keyword;
	
	/**
	 * type默认为10
	 */
	private int type = 10;
	
	/**
	 * status 默认为1；
	 */
	private int status = 1;
	
	private Timestamp lastopTime;
	 
	private Timestamp createTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Timestamp getLastopTime() {
		return lastopTime;
	}

	public void setLastopTime(Timestamp lastopTime) {
		this.lastopTime = lastopTime;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	
}
