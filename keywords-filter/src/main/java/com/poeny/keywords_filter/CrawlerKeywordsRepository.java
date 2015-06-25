package com.poeny.keywords_filter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.poeny.keywords_filter.keywords_accessor.SubjectKeywordsAccessor;
import com.poeny.keywords_filter.manager.ConnectionManager;
import com.poeny.keywords_filter.tool.Sets;

/**
 * 更新至crawlerDB时的Database Access Operator
 * @author BAO
 *
 */
public class CrawlerKeywordsRepository {

	private static CrawlerKeywordsRepository instance;

	private static final String DEL_REMOTE = "DELETE FROM wdyq_keyword_design where keyword=? and type=?";

	private static final String UPDATE_REMOTE_OPTIME = "UPDATE wdyq_keyword_design SET lastoptime=? where keyword=?";

	private static final String INSERT_REMOTE = "INSERT INTO wdyq_keyword_design ( keyword, type, status, lastoptime, createtime ) VALUES ( ?,?,?,?,? )";

	private ExecutorService threadPool = Executors.newFixedThreadPool(10);

	private static ConnectionManager connManager = ConnectionManager
			.getInstance();

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SubjectKeywordsAccessor.class);

	public static CrawlerKeywordsRepository getInstance() {
		if (instance == null) {
			instance = new CrawlerKeywordsRepository();
		}
		return instance;
	}

	

	public void updateOpTimeAll(Set<String> words) throws SQLException {

		int count = 0;
		Connection conn = connManager.getCrawlerDBConnection();
		try {
			PreparedStatement ps = conn.prepareStatement(UPDATE_REMOTE_OPTIME);
			try {
				for (String word : words) {
					try {

						ps.setTimestamp(1,
								new Timestamp(System.currentTimeMillis()));
						ps.setString(2, word);
						ps.addBatch();
						if ((count++) % 100 == 0) {
							ps.executeBatch();
						}
					} catch (SQLException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
			} finally {
				ps.close();
			}
		} finally {
			LOGGER.info("更新keyword操作时间" + count + "条！");
			conn.close();
		}
	}

	/**
	 * 删除所有应该删除的信息
	 * 
	 * @param words
	 * @param type
	 */
	public void deleteAll(Set<String> words, int type) throws SQLException {
		for (String word : words) {
			threadPool.submit(new KeywordsDeleteThread(word, type));
		}
	}

	/**
	 * 将set中所有的keyword插入到远程数据库
	 * @param words
	 * @param type
	 * @throws SQLException
	 */
	public void insertAll(Set<String> words, int type) throws SQLException {
		for (String word : words) {
			threadPool.submit(new KeywordsInsertThread(word, type));
		}
	}

	class KeywordsDeleteThread implements Runnable {

		private String keyword;
		private int type;

		KeywordsDeleteThread(String keyword, int t) {
			this.keyword = keyword;
			this.type = t;
		}

		@Override
		public void run() {
			try {
				Connection conn = connManager.getCrawlerDBConnection();
				try {
					PreparedStatement ps = conn.prepareStatement(DEL_REMOTE);
					try {
						int p = 1;
						ps.setString(p++, keyword);
						ps.setInt(p++, type);
						ps.executeUpdate();
					} finally {
						ps.close();
					}
				} finally {
					conn.close();
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	class KeywordsInsertThread implements Runnable {

		private String keyword;
		private int type;

		KeywordsInsertThread(String keyword, int t) {
			this.keyword = keyword;
			this.type = t;
		}

		@Override
		public void run() {
			try {
				Connection conn = connManager.getCrawlerDBConnection();
				try {
					PreparedStatement ps = conn.prepareStatement(INSERT_REMOTE);
					try {
						int p = 1;
						ps.setString(p++, keyword);
						ps.setInt(p++, type);// TODO
						ps.setInt(p++, 1);
						ps.setTimestamp(p++,
								new Timestamp(System.currentTimeMillis()));
						ps.setTimestamp(p++,
								new Timestamp(System.currentTimeMillis()));
						ps.executeUpdate();
					} finally {
						ps.close();
					}
				} finally {
					conn.close();
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}


	/**
	 * 测试插入数据的速度
	 */
	public void testInsert() {
		for (int i = 0; i < 100; i++) {
			threadPool.submit(new KeywordsInsertThread("test", 100));
		}
	}

}
