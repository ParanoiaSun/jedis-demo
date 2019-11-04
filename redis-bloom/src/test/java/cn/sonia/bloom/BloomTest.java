package cn.sonia.bloom;

import cn.sonia.bloom.builder.BloomFilterBuilder;
import cn.sonia.bloom.hash.CRC32HashFunction;
import cn.sonia.bloom.hash.HashFunction;
import redis.clients.jedis.JedisCluster;

/**
 * Create By Sonia_Sun on 2019-11-04
 */
public class BloomTest {

    public static void main(String[] args) {

        // rediscluster客户端
        long appId = 10400;
        //初始化...
        JedisCluster jedisCluster = null;
        // 布隆过滤器名
        String bloomFilterName = "cc-bloom-filter";
        // 预计插入条数(例如1个亿)
        long expectedInsertions = 100000000;
        // 预计错误率(例如万分之一)
        double falseProbability = 0.0001;

        HashFunction crc32 = new CRC32HashFunction();
        BloomFilter<String> bloomFilter = new BloomFilterBuilder(jedisCluster, bloomFilterName, expectedInsertions, falseProbability)
                .setHashFunction(crc32)
                .build();

        // 添加
        bloomFilter.add("a");
        bloomFilter.add("b");
        bloomFilter.add("c");
        bloomFilter.add("d");

        // 包含检测
        // true
        System.out.println(bloomFilter.contains("c"));
        // false
        System.out.println(bloomFilter.contains("zz"));

    }

}
