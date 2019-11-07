package cn.sonia.bloom;

import cn.sonia.bloom.builder.BloomFilterBuilder;
import cn.sonia.bloom.hash.CRC32HashFunction;
import cn.sonia.bloom.hash.HashFunction;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * Create By Sonia_Sun on 2019-11-04
 */
public class BloomTest {

    public static void main(String[] args) {

        // rediscluster客户端
        long appId = 10400;

        Set<HostAndPort> nodeList = new HashSet<HostAndPort>();
        nodeList.add(new HostAndPort("127.0.0.1", 7000));
        nodeList.add(new HostAndPort("127.0.0.1", 7001));
        nodeList.add(new HostAndPort("127.0.0.1", 7002));
        nodeList.add(new HostAndPort("127.0.0.1", 7003));
        nodeList.add(new HostAndPort("127.0.0.1", 7004));
        nodeList.add(new HostAndPort("127.0.0.1", 7005));

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //初始化...
        JedisCluster jedisCluster = new JedisCluster(nodeList, 1000, jedisPoolConfig);
        // 布隆过滤器名
        String bloomFilterName = "sonia-bloom-filter";
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
