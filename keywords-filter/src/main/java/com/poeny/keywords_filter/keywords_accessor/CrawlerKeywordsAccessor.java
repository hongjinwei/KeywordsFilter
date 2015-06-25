package com.poeny.keywords_filter.keywords_accessor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.poeny.keywords_filter.manager.ConnectionManager;

public class CrawlerKeywordsAccessor {

	private static final String QUERY_NOT_EQUAL = "select * from wdyq_keyword_design where type<>?";

	private static final String QUERY_REMOTE_ALL = "select * from wdyq_keyword_design";

	private static final String QUERY_REMOTE_BY_TYPE = "select * from wdyq_keyword_design where type=?";

	private static ConnectionManager connManager = ConnectionManager.getInstance();

	private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerKeywordsAccessor.class);

	private static CrawlerKeywordsAccessor instance;

	public static CrawlerKeywordsAccessor getInstance() {
		if (instance == null) {
			instance = new CrawlerKeywordsAccessor();
		}
		return instance;
	}

	/**
	 * 主要用于获取远程类型不为type的关键字的set
	 * 
	 * @param type
	 * @return
	 */
	public Set<String> getKeywordsTypeNotEqualTo(int type) throws SQLException {

		Set<String> ans = new HashSet<String>();
		Connection conn = connManager.getCrawlerDBConnection();
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_NOT_EQUAL);
			try {
				ps.setInt(1, type);
				ResultSet res = ps.executeQuery();
				while (res.next()) {
					String word = res.getString("keyword");
					if (!word.equals("") || word != null) {
						ans.add(word);
					}
				}
				res.close();
			} finally {
				ps.close();
			}
		} catch (SQLException e) {
			LOGGER.error(e.getMessage() + "取远程type不为" + type + "的取数据失败！", e);
		} finally {
			conn.close();
		}
		return ans;
	}

	/**
	 * 获取crawler用的SQL表中所有的关键字
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Set<String> getAllKeywords() throws SQLException {
		Connection conn = connManager.getCrawlerDBConnection();
		Set<String> ans = new HashSet<String>();
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_REMOTE_ALL);
			try {
				ResultSet res = ps.executeQuery();
				while (res.next()) {
					String word = res.getString("keyword");
					if (!word.equals("") || word != null) {
						ans.add(word);
					}
				}
			} finally {
				ps.close();
			}
		} finally {
			conn.close();
		}
		return ans;
	}

	/**
	 * 获取crawler用的SQL表中所有类型为type的关键字
	 * 
	 * @param type
	 * @return
	 * @throws SQLException
	 */
	public Set<String> getKeywordsByType(int type) throws SQLException {
		Connection conn = connManager.getCrawlerDBConnection();
		Set<String> ans = new HashSet<String>();
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_REMOTE_BY_TYPE);
			try {
				ps.setInt(1, type);
				ResultSet res = ps.executeQuery();
				while (res.next()) {
					String word = res.getString("keyword");
					if (!word.equals("") || word != null) {
						ans.add(word);
					}
				}
			} finally {
				ps.close();
			}
		} catch (SQLException e) {
			LOGGER.error(e.getMessage() + "按type取数据失败！", e);
			return null;
		} finally {
			conn.close();
		}

		return ans;
	}

}
