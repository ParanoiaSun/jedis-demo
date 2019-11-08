package cn.sonia.redis.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;

/**
 * Create By Sonia_Sun on 2019-11-08
 */
public class JedisPoolFactory {

    private JedisPool jedisPool;

    private Logger logger = LoggerFactory.getLogger(JedisPoolFactory.class);

    public void init() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPool = new JedisPool();
    }

    public void destroy() {
        if(jedisPool != null) {
            jedisPool.close();
        }
    }
}
