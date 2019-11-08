package cn.sonia.redis.service;

/**
 * Create By Sonia_Sun on 2019-11-05
 */
public interface RedisService {

    String set(String key, String value);

    String get(String key);
}
