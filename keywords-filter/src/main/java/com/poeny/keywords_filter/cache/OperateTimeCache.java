package com.poeny.keywords_filter.cache;

import java.sql.Timestamp;

public class OperateTimeCache {

	private static long  lastoptime = 0;
	
	public static Timestamp getLastOpTime() {
		return new Timestamp(lastoptime);
	}
	
	public static void setLastOpTime() {
		lastoptime = System.currentTimeMillis();
	}
	
	public static Timestamp getAndUpdateLastOpTime() {
		long last = lastoptime;
		setLastOpTime();
		return new Timestamp(last);
	}
}
