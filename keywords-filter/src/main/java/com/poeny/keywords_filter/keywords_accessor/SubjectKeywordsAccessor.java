package com.poeny.keywords_filter.keywords_accessor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.poeny.keywords_filter.Filter;
import com.poeny.keywords_filter.manager.ConnectionManager;
import com.poeny.keywords_filter.model.QuerySubjectResult;
import com.poeny.keywords_filter.tool.WordsUtil;

/**
 * 获取用户定义的关键字
 * 
 * @author BAO
 *
 */
public class SubjectKeywordsAccessor {

	private static ConnectionManager connManager = ConnectionManager.getInstance();

	private static final Logger LOGGER = LoggerFactory.getLogger(SubjectKeywordsAccessor.class);

	private static final String QUERY_STRING = "SELECT keywords from pe_t_subject_keywords ORDER BY id ASC LIMIT ?,?";

	private static final String QUERY_ALL = "SELECT keywords from pe_t_subject_keywords ORDER BY id ASC";

	private static SubjectKeywordsAccessor instance = null;

	public static SubjectKeywordsAccessor getInstance() {
		if (instance == null) {
			instance = new SubjectKeywordsAccessor();
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
	public Set<String> getKeywords(int start, int number) throws SQLException {
		Set<String> result = new HashSet<String>();
		Connection conn = connManager.getSubjectDBConnection();
		try {
			PreparedStatement prepareStatement = conn.prepareStatement(QUERY_STRING);
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
	 * 取出用户数据库所有的关键字
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Set<String> getAllKeywords() throws SQLException {
		Set<String> result = new HashSet<String>();
		Connection conn = connManager.getSubjectDBConnection();
		try {
			PreparedStatement prepareStatement = conn.prepareStatement(QUERY_ALL);
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

	/**
	 * 执行preparedStatement, 获取用户定义的keywords SQL中所有数据的keywords字段，装载成一个Set
	 * 
	 * @param ps
	 * @return
	 * @throws SQLException
	 */
	private Set<String> query(PreparedStatement ps) throws SQLException {
		ResultSet rs = ps.executeQuery();
		Set<String> result = new HashSet<String>();
		try {
			while (rs.next()) {
				String keywordString = rs.getString("keywords");
				String[] keywords = keywordString.split(" ");
				for (int i = 0; i < keywords.length; i++) {
					String keyword = WordsUtil.cleanWord(keywords[i]);
					if (!keyword.equals("") || keyword != null) {
						result.add(keyword);
					}
				}
			}
		} finally {
			rs.close();
		}
		return result;
	}

	/**
	 * 根据更新时间获取pe_t_subject 和 pe_t_subject_keywords表里的所有内容 其中：更新时间大于timestamp参数
	 * 并且pe_t_subject_keywords表里rejectId不能为1
	 * 
	 * @param timestamp
	 * @throws SQLException
	 */
	public QuerySubjectResult getSubjectByLastoptime(Timestamp timestamp) throws SQLException {
		String sql = "select * from pe_t_subject a JOIN pe_t_subject_keywords b on a.id=b.subjectId and a.update_time > ? and b.rejectFlag=0";
		QuerySubjectResult result = new QuerySubjectResult();
		Connection conn = connManager.getSubjectDBConnection();
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			try {
				ps.setTimestamp(1, timestamp);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					int subjectId = rs.getInt("subjectId");
					int state = rs.getInt("state");
					Timestamp updateTime = rs.getTimestamp("update_time");
					String keywords = rs.getString("keywords");
					if (keywords == null) {
						continue;
					}
					Set<String> kwSet = WordsUtil.getSubjectKeywordSetFromRawstr(keywords);
					kwSet = Filter.getFilter().filterWords(kwSet);
					result.add(subjectId, state, updateTime, kwSet);
				}
				return result;
			} catch (SQLException e) {
				LOGGER.error(e.getMessage(), e);
			} finally {
				ps.close();
			}
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			conn.close();
		}
		return null;
	}

}
