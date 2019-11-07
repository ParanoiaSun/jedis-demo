package cn.sonia.redis.service;

import redis.clients.jedis.JedisCluster;

/**
 * Create By Sonia_Sun on 2019-11-08
 */
public class RedisClusterServiceImpl implements RedisService{

    private JedisCluster jedisCluster;

    public String set(String key, String value) {
        System.out.println(jedisCluster);
        return jedisCluster.set(key, value);
    }

    public String get(String key) {
        return jedisCluster.get(key);
    }

    public void setJedisCluster(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }
}
