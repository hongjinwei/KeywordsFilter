package com.poeny.keywords_filter.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 计时器
 * 
 * @author BAO
 */
public class TimeCounter {

	long e1 = 0;
	long e2 = 0;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TimeCounter.class);

	public void start() {
		this.e1 = System.currentTimeMillis();
	}

	public void end() {
		this.e2 = System.currentTimeMillis();
	}

	public void show(String msg) {
		if(e1 == 0 || e2 == 0) {
			LOGGER.error("计时器没有start或者end！");
			return ;
		}
		LOGGER.info(msg + " 耗时 ： " + (e2 - e1) + "ms");
	}

	public void show() {
		LOGGER.info("耗时：" + (e2 - e1) + "ms");
	}
}
