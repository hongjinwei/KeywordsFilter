package com.poeny.keywords_filter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Updater {

	private static Updater instance;

	private static final String QUERY_REMOTE_ALL = "select * from wdyq_keyword_design";

	private static final String QUERY_REMOTE_BY_TYPE = "select * from wdyq_keyword_design where type=?";

	private static final String DEL_REMOTE = "DELETE FROM wdyq_keyword_design where keyword=? and type=?";

	private static final String UPDATE_REMOTE_OPTIME = "UPDATE wdyq_keyword_design SET lastoptime=? where keyword=?";

	private static final String INSERT_REMOTE = "INSERT INTO wdyq_keyword_design ( keyword, type, status, lastoptime, createtime ) VALUES ( ?,?,?,?,? )";

	private static final int TYPE = 10;

	private static ConnectionManager connManager = ConnectionManager
			.getInstance();

	private static final Logger LOGGER = LoggerFactory
			.getLogger(Keywords.class);

	public static Updater getInstance() {
		if (instance == null) {
			instance = new Updater();
		}
		return instance;
	}

	public Set<String> getRemoteSet() throws SQLException {
		Connection conn = connManager.getWriteConnection();
		Set<String> ans = new HashSet<String>();
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_REMOTE_ALL);
			try {
				ResultSet res = ps.executeQuery();
				while (res.next()) {
					ans.add(res.getString("keyword"));
				}
			} finally {
				ps.close();
			}
		} finally {
			conn.close();
		}
		return ans;
	}

	public Set<String> getRemoteSetByType(int type) throws SQLException {
		Connection conn = connManager.getWriteConnection();
		Set<String> ans = new HashSet<String>();
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_REMOTE_BY_TYPE);
			try {
				ps.setInt(1, TYPE);
				ResultSet res = ps.executeQuery();
				while (res.next()) {
					ans.add(res.getString("keyword"));
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

	private void updateOpTimeAll(Set<String> words){
		Connection conn = connManager.getWriteConnection();
		int count = 0;
		try{
			PreparedStatement ps = conn.prepareStatement(UPDATE_REMOTE_OPTIME);
			try{
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
			}finally{
				ps.close();
			}
		}catch(SQLException e){
			LOGGER.error(e.getMessage(), e);
		}finally{
			try {
				conn.close();
			} catch (SQLException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}	
	}

	private void deleteAll(Set<String> words, int type) {
		Connection conn = connManager.getWriteConnection();
		int count = 0;
		try {
			PreparedStatement ps = conn.prepareStatement(DEL_REMOTE);
			try {
				for (String word : words) {
					try {
						int p = 1;
						ps.setString(p++, word);
						ps.setInt(p++, type);
						ps.addBatch();
						if ((count++) % 100 == 0) {
							ps.executeBatch();
						}
					} catch (SQLException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
				ps.executeBatch();
			} finally {
				ps.close();
			}
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			LOGGER.info("删除数据 " + count + "条！");
			try {
				conn.close();
			} catch (SQLException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	private void insertAll(Set<String> words, int type) {
		Connection conn = connManager.getWriteConnection();
		int count = 0;
		try {
			PreparedStatement ps = conn.prepareStatement(INSERT_REMOTE);
			try {
				for (String word : words) {
					try {
						int p = 1;
						ps.setString(p++, word);
						ps.setInt(p++, type);
						ps.setInt(p++, 1);
						ps.setTimestamp(p++,
								new Timestamp(System.currentTimeMillis()));
						ps.setTimestamp(p++,
								new Timestamp(System.currentTimeMillis()));
						ps.addBatch();
						if ((count++) % 100 == 0) {
							ps.executeBatch();
						}
					} catch (SQLException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
				ps.executeBatch();
			} finally {
				ps.close();
				conn.close();
			}
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			LOGGER.info("插入数据 " + count + "条！");
			try {
				conn.close();
			} catch (SQLException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	private void updateAll(Set<String> remoteSet, Set<String> myTypeSet,Set<String> newSet) {
		long e1, e2;
		e1 = System.currentTimeMillis();
		Set<String> shouldUpdateTime = Sets.intersection(remoteSet, newSet);
		Set<String> shouldDeleted = Sets.difference(myTypeSet, newSet);
		Set<String> shouldInserted = Sets.difference(newSet, remoteSet);

		e2 = System.currentTimeMillis();
		System.out.println("计算耗时：" + (e2 - e1) + "ms");

//		e1 = System.currentTimeMillis();
//		 updateOpTimeAll(conn, shouldUpdateTime);
//		e2 = System.currentTimeMillis();
//		System.out.println("update耗时：" + (e2 - e1) + "ms");

		e1 = System.currentTimeMillis();
		deleteAll(shouldDeleted, TYPE);
		e2 = System.currentTimeMillis();
		System.out.println("delete耗时：" + (e2 - e1) + "ms");

		e1 = System.currentTimeMillis();
		insertAll(shouldInserted, TYPE);
		e2 = System.currentTimeMillis();
		System.out.println("insert耗时：" + (e2 - e1) + "ms");
	}

	public void testInsert() {
		Connection conn = connManager.getWriteConnection();
		int count = 0;
		try {
			PreparedStatement ps = conn.prepareStatement(INSERT_REMOTE);
			try {
				for (int i = 0; i < 100; i++) {
					try {
						int p = 1;
						ps.setString(p++, "test");
						ps.setInt(p++, 100);
						ps.setInt(p++, 1);
						ps.setTimestamp(p++,
								new Timestamp(System.currentTimeMillis()));
						ps.setTimestamp(p++,
								new Timestamp(System.currentTimeMillis()));
						ps.addBatch();
						if ((count++) % 100 == 0) {
							ps.executeBatch();
						}
					} catch (SQLException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
				ps.executeBatch();
			} finally {
				ps.close();
				conn.close();
			}
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			LOGGER.info("插入数据 " + count + "条！");
			try {
				conn.close();
			} catch (SQLException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * 将已经过滤的本地set更新到远程数据库
	 * 
	 * @param set
	 */
	public void updateToRemote(Set<String> set) {
		Set<String> remoteSet;
		Set<String> myTypeSet;
		try {
			remoteSet = getRemoteSet();
			myTypeSet = getRemoteSetByType(10);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage() + "读取keyword_design表失败", e);
			return;
		}
		updateAll(remoteSet, myTypeSet, set);
	}
}
