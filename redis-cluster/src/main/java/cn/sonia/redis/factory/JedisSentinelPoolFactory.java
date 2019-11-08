package cn.sonia.redis.factory;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import java.util.Set;

/**
 * Create By Sonia_Sun on 2019-11-08
 */
public class JedisSentinelPoolFactory {

    private JedisSentinelPool sentinelPool;

    private Set<String> sentinelSet;

    private String masterName;

    private int timeout;

    public void init() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        sentinelPool = new JedisSentinelPool(masterName, sentinelSet, jedisPoolConfig, timeout);
    }

    public void destroy() {
        if(sentinelPool != null) {
            sentinelPool.close();
        }
    }

    public JedisSentinelPool getSentinelPool() {
        return sentinelPool;
    }

    public void setSentinelSet(Set<String> sentinelSet) {
        this.sentinelSet = sentinelSet;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
