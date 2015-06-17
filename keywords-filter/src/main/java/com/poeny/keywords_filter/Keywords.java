package com.poeny.keywords_filter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Keywords {

	private static ConnectionManager connManager = ConnectionManager
			.getInstance();

	private static final Logger LOGGER = LoggerFactory
			.getLogger(Keywords.class);

	private static final String QUERY_STRING = "SELECT keywords from pe_t_subject_keywords ORDER BY id ASC LIMIT ?,?";

	private static final String QUERY_ALL = "SELECT keywords from pe_t_subject_keywords ORDER BY id ASC";

	private static final String QUERY_NOT_EQUAL = "select * from wdyq_keyword_design where type<>?";

	private static Keywords instance = null;

	public static Keywords getInstance() {
		if (instance == null) {
			instance = new Keywords();
		}
		return instance;
	}

	/**
	 * 返回从start开始的number条数据的所有keywords
	 * 
	 * @param start
	 * @param number
	 * @return
	 * @throws SQLException
	 */
	public Set<String> get(int start, int number) throws SQLException {
		Set<String> result = new HashSet<String>();
		Connection conn = connManager.getReadConnection();
		try {
			PreparedStatement prepareStatement = conn
					.prepareStatement(QUERY_STRING);
			try {
				int p = 1;
				prepareStatement.setInt(p++, start);
				prepareStatement.setInt(p++, number);
				result.addAll(query(prepareStatement));
				return result;
			} finally {
				prepareStatement.close();
			}
		} finally {
			conn.close();
		}
	}

	/**
	 * 取出所有的关键字
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Set<String> getAll() throws SQLException {
		Set<String> result = new HashSet<String>();
		Connection conn = connManager.getReadConnection();
		try {
			PreparedStatement prepareStatement = conn
					.prepareStatement(QUERY_ALL);
			try {
				Set<String> tmp = query(prepareStatement);
				result.addAll(tmp);
				return result;
			} finally {
				prepareStatement.close();
			}
		} finally {
			conn.close();
		}
	}

	private Set<String> query(PreparedStatement ps) throws SQLException {
		ResultSet rs = ps.executeQuery();
		Set<String> result = new HashSet<String>();
		try {
			while (rs.next()) {
				String keywordString = rs.getString("keywords");
				String[] keywords = keywordString.split(" ");
				for (int i = 0; i < keywords.length; i++) {
					String keyword = cleanWord(keywords[i]);
					if (!keyword.equals("")) {
						result.add(keyword);
					}
				}
			}
		} finally {
			rs.close();
		}
		return result;
	}

	public String cleanWord(String word) {
		if (word == null || word.length() <= 1) {
			return "";
		}
		word = word.replaceAll("\\s+", "");
		return word;
	}

	public Set<String> getRemoteTypeNotEqualTo(int type) {
		Connection conn = connManager.getWriteConnection();
		Set<String> ans = new HashSet<String>();

		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_NOT_EQUAL);
			try {
				ps.setInt(1, type);
				ResultSet res = ps.executeQuery();
				while (res.next()) {
					ans.add(res.getString("keyword"));
				}
			} finally {
				ps.close();
			}
		} catch (SQLException e) {
			LOGGER.error(e.getMessage() + "取远程type不为" + type + "的取数据失败！", e);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}

		return ans;
	}
}
