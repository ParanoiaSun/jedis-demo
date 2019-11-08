package cn.sonia.redis.service;

import redis.clients.jedis.JedisPool;

/**
 * Create By Sonia_Sun on 2019-11-08
 */
public class RedisServiceImpl implements RedisService{

    private JedisPool jedisPool;

    public String set(String key, String value) {
        return jedisPool.getResource().set(key, value);
    }

    public String get(String key) {
        return jedisPool.getResource().get(key);
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }
}
