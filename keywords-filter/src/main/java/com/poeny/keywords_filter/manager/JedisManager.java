package com.poeny.keywords_filter.manager;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisManager {

	private static JedisPool pool = null;
	
	public static JedisPool getJedisPool() {
		if(pool == null ){
			JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxIdle(500);
            config.setMaxIdle(5);
            config.setMaxWaitMillis(1000 * 100);
            config.setTestOnBorrow(true);
            pool = new JedisPool(config, "192.168.2.191", 8888);
		}
		return pool;
	}
	
	public static void returnResource(JedisPool pool, Jedis redis){
		if(redis != null){
			pool.returnResource(redis);
		}
	}
	
}
