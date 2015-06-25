package com.poeny.keywords_filter;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.poeny.keywords_filter.cache.SubjectKeywordsCache;
import com.poeny.keywords_filter.cache.CrawlerKeywordsCache;
import com.poeny.keywords_filter.cache.KeywordsFreqCache;
import com.poeny.keywords_filter.cache.OperateTimeCache;
import com.poeny.keywords_filter.keywords_accessor.CrawlerKeywordsAccessor;
import com.poeny.keywords_filter.keywords_accessor.SubjectKeywordsAccessor;
import com.poeny.keywords_filter.manager.ConnectionManager;
import com.poeny.keywords_filter.model.QuerySubjectResult;
import com.poeny.keywords_filter.model.SubjectKeywords;
import com.poeny.keywords_filter.model.UpdateCacheResult;
import com.poeny.keywords_filter.tool.Sets;
import com.poeny.keywords_filter.tool.TimeCounter;
import com.poeny.keywords_filter.updater.CacheUpdater;
import com.poeny.keywords_filter.updater.CrawlerKeywordUpdater;

/**
 * Hello world!
 *
 */
public class App {

	private static CrawlerKeywordUpdater updater = CrawlerKeywordUpdater.getInstance();
	private static CrawlerKeywordsRepository crawlerRepo = CrawlerKeywordsRepository.getInstance();
	private static SubjectKeywordsAccessor subjectKeywords = SubjectKeywordsAccessor.getInstance();
	private static CrawlerKeywordsAccessor crawlerKeywords = CrawlerKeywordsAccessor.getInstance();
	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
	private static TimeCounter tc = new TimeCounter();

	private static void init() {
		ConnectionManager.getInstance().init();
		CrawlerKeywordsCache.getCache().init();
	}

	static {
		init();
	}

	private static void run() {
		List<SubjectKeywords> updatedSubjectKeywords, deletedSubjectkeywords;
		QuerySubjectResult result = new QuerySubjectResult();
		tc.start();
		try {
			result = subjectKeywords.getSubjectByLastoptime(OperateTimeCache.getAndUpdateLastOpTime());
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return;
		}

		tc.end();
		tc.show("查询完毕");
		if (result == null) {
			return;
		}

		updatedSubjectKeywords = result.getUpdatedSubjectKeywords();
		deletedSubjectkeywords = result.getDeletedSubjectKeywords();

		tc.start();
		// 更新词频
		CacheUpdater.updateSubjectKeyWordsFreq(updatedSubjectKeywords, deletedSubjectkeywords);
		tc.end();
		tc.show("更新词频");

		tc.start();
		// 选择一个要更新的单词set
		Chooser.chooseAll(updatedSubjectKeywords);
		tc.end();
		tc.show("选择一个要更新的单词set");

		tc.start();
		UpdateCacheResult ans = CacheUpdater.updateCache(updatedSubjectKeywords, deletedSubjectkeywords);
		tc.end();
		tc.show("更新cache");

		tc.start();

		LOGGER.info("更新的Subject个数：" + updatedSubjectKeywords.size());
		LOGGER.info("删除的Subject个数" + deletedSubjectkeywords.size());
		System.out.println("应该删除： " + ans.getShouldDeleted());
		System.out.println("应该插入: " + ans.getShouldInsert());
		SubjectKeywordsCache.getCache().show();
		CrawlerKeywordsCache.getCache().show();
		KeywordsFreqCache.getCache().show();
		
		CrawlerKeywordsRepository crawlerRepo = CrawlerKeywordsRepository.getInstance();
		try {
			crawlerRepo.deleteAll(ans.getShouldDeleted(), 10);
			crawlerRepo.insertAll(ans.getShouldInsert(), 10);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
		}
		tc.end();
		tc.show("更新到远程crawler表");
		// 最后再清理
		result.close();
	}

	public static void start() {
		TimeCounter tc1 = new TimeCounter();
		TimeCounter tc2 = new TimeCounter();
		Set<String> s, t;
		tc1.start();
		tc2.start();
		try {
			s = subjectKeywords.getAllKeywords();
			t = crawlerKeywords.getKeywordsTypeNotEqualTo(10);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage() + "获取数据失败！ 不作后续处理！", e);
			return;
		}
		tc1.end();
		tc1.show("获取初始数据");

		tc1.start();
		Set<String> set = Sets.union(s, t);
		LOGGER.info("初始单词个数 ： " + set.size());
		Filter.getFilter().filterWords(set);
		LOGGER.info("过滤后单词个数 ： " + set.size());
		tc1.end();
		tc1.show("过滤词汇");

		tc1.start();
		try {
			updater.updateToRemote(set);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage() + "update到远程数据库失败！", e);
		}
		tc1.end();
		tc1.show("更新数据至远程数据库");

		tc2.end();
		tc2.show("全过程结束");
	}

	public static void testInsert() {
		TimeCounter tc = new TimeCounter();
		tc.start();
		crawlerRepo.testInsert();
		tc.end();
		tc.show();
	}

	public static void test2() {
		Set<String> a = new HashSet<String>(), b = new HashSet<String>();
		a.add("暴动");
		a.add("机会很好");
		b.add("暴动时候");
		b.add("机会");
		b.add("有时候");
		a = Filter.getFilter().filterWords(a, b);
		System.out.println(a);
	}

	public static void main(String[] args) {
		// start();
		while (true) {

			run();
			try {
				Thread.sleep(20 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
