package com.poeny.keywords_filter.updater;

import java.sql.SQLException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.poeny.keywords_filter.CrawlerKeywordsRepository;
import com.poeny.keywords_filter.keywords_accessor.CrawlerKeywordsAccessor;
import com.poeny.keywords_filter.tool.Sets;
import com.poeny.keywords_filter.tool.TimeCounter;

/**
 * 计算应该更新信息的类
 * 
 * @author BAO
 *
 */
public class CrawlerKeywordUpdater {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerKeywordUpdater.class);

	private static CrawlerKeywordsRepository crawlerRepo = CrawlerKeywordsRepository.getInstance();

	private static CrawlerKeywordsAccessor crawlerKeywords = CrawlerKeywordsAccessor.getInstance();

	private static TimeCounter timer = new TimeCounter();

	private static final int TYPE = 10;

	private static CrawlerKeywordUpdater instance;

	public static CrawlerKeywordUpdater getInstance() {
		if (instance == null) {
			instance = new CrawlerKeywordUpdater();
		}
		return instance;
	}

	/**
	 * 将已经过滤的本地set更新到远程数据库
	 * 
	 * 更新所有的keyword，需要删除的为 remote表中 type=10 的keyword的set
	 * 去除掉本次计算之后的set中所有元素后，剩下的就是需要去除掉的keyword
	 * 
	 * @param filteredSet
	 */
	public void updateToRemote(Set<String> filteredSet) throws SQLException {
		Set<String> crawlerKeywordsAll, myTypeCrawlerKeywords;
		try {
			crawlerKeywordsAll = crawlerKeywords.getAllKeywords();
			myTypeCrawlerKeywords = crawlerKeywords.getKeywordsByType(TYPE);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage() + "读取keyword_design表失败", e);
			throw new SQLException("读取keyword_design表失败");
		}

		Set<String> shouldDeleted = Sets.difference(myTypeCrawlerKeywords, filteredSet);
		Set<String> shouldInserted = Sets.difference(filteredSet, crawlerKeywordsAll);

		timer.start();
		try {
			crawlerRepo.deleteAll(shouldDeleted, TYPE);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage() + "删除数据出现异常！", e);
		}
		timer.end();
		timer.show("删除数据完成");

		timer.start();
		try {
			crawlerRepo.insertAll(shouldInserted, TYPE);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage() + "插入数据出现异常！", e);
		}
		timer.end();
		timer.show("插入数据完成");
	}

}
