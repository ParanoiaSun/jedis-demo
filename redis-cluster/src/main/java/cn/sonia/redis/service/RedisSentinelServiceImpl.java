package cn.sonia.redis.service;

import redis.clients.jedis.JedisSentinelPool;

/**
 * Create By Sonia_Sun on 2019-11-08
 */
public class RedisSentinelServiceImpl implements RedisService{

    private JedisSentinelPool sentinelPool;

    public String set(String key, String value) {
        return sentinelPool.getResource().set(key, value);
    }

    public String get(String key) {
        return sentinelPool.getResource().get(key);
    }

    public void setSentinelPool(JedisSentinelPool sentinelPool) {
        this.sentinelPool = sentinelPool;
    }
}
