package cn.sonia.bloom.impl;

import cn.sonia.bloom.BloomFilter;
import cn.sonia.bloom.builder.BloomFilterBuilder;
import cn.sonia.bloom.hash.HashFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.util.JedisClusterCRC16;

import java.util.*;

/**
 * Bloom过滤器具体实现
 * Create By Sonia_Sun on 2019-11-04
 */
public class BloomFilterImpl<T> implements BloomFilter<T> {
    private Logger logger = LoggerFactory.getLogger(BloomFilterImpl.class);

    private BloomFilterBuilder config;

    public BloomFilterImpl(BloomFilterBuilder bloomFilterBuilder) {
        this.config = bloomFilterBuilder;
    }

    public boolean add(T object) {
        if (object == null) {
            return false;
        }
        // 偏移量列表
        List<Integer> offsetList = hash(object);
        if (offsetList == null || offsetList.isEmpty()) {
            return false;
        }
        String key = genBloomFilterDistributeKey(object);
        return pipelineSetBit(key, new HashSet<Integer>(offsetList));
    }

    public boolean batchAdd(List<T> objectList) {
        if (objectList == null || objectList.isEmpty()) {
            return false;
        }
        Map<String, Set<Integer>> keyOffsetSetMap = new HashMap<String, Set<Integer>>();
        for (T object : objectList) {
            // 偏移量列表
            List<Integer> offsetList = hash(object);
            if (offsetList == null || offsetList.isEmpty()) {
                continue;
            }
            String key = genBloomFilterDistributeKey(object);
            if (keyOffsetSetMap.containsKey(key)) {
                keyOffsetSetMap.get(key).addAll(offsetList);
            } else {
                Set<Integer> offsetSet = new HashSet<Integer>(offsetList);
                keyOffsetSetMap.put(key, offsetSet);
            }
        }
        for (Map.Entry<String, Set<Integer>> entry : keyOffsetSetMap.entrySet()) {
            String key = entry.getKey();
            Set<Integer> offsetSet = entry.getValue();
            pipelineSetBit(key, offsetSet);
        }
        return true;
    }

    public boolean contains(T object) {
        if (object == null) {
            return false;
        }
        // 偏移量列表
        List<Integer> offsetList = hash(object);
        if (offsetList == null || offsetList.isEmpty()) {
            return false;
        }
        String key = genBloomFilterDistributeKey(object);
        // 获取位图值，只要有非true的就证明不包含
        Map<Integer, Boolean> offsetResultMap = pipelineGetBit(key, offsetList);
        for (Boolean bit : offsetResultMap.values()) {
            if (bit == null || !bit) {
                return false;
            }
        }
        return true;
    }

    public Map<T, Boolean> batchContains(List<T> objectList) {
        if (objectList == null || objectList.isEmpty()) {
            return Collections.emptyMap();
        }
        // 最终结果
        Map<T, Boolean> resultMap = new HashMap<T, Boolean>();

        // 按照object和offsetList做分组
        Map<T, List<Integer>> objectOffsetListMap = new HashMap<T, List<Integer>>();
        Map<String, List<Integer>> keyOffsetSetMap = new HashMap<String, List<Integer>>();

        // 分组
        for (T object : objectList) {
            List<Integer> offsetList = hash(object);
            if (offsetList == null || offsetList.isEmpty()) {
                continue;
            }
            String key = genBloomFilterDistributeKey(object);
            if (keyOffsetSetMap.containsKey(key)) {
                keyOffsetSetMap.get(key).addAll(offsetList);
            } else {
                List<Integer> offsetListTemp = new ArrayList<Integer>(offsetList);
                keyOffsetSetMap.put(key, offsetListTemp);
            }
            objectOffsetListMap.put(object, offsetList);
        }

        Map<Integer, Boolean> totalOffsetResultMap = new HashMap<Integer, Boolean>();
        for (Map.Entry<String, List<Integer>> entry : keyOffsetSetMap.entrySet()) {
            String key = entry.getKey();
            List<Integer> offsetList = entry.getValue();
            Map<Integer, Boolean> offsetResultMap = pipelineGetBit(key, offsetList);
            totalOffsetResultMap.putAll(offsetResultMap);
        }

        for (Map.Entry<T, List<Integer>> entry : objectOffsetListMap.entrySet()) {
            T object = entry.getKey();
            List<Integer> offsetList = entry.getValue();
            boolean result = true;
            for (Integer offset : offsetList) {
                Boolean t = totalOffsetResultMap.get(offset);
                if (t == null || !t) {
                    result = false;
                    break;
                }
            }
            resultMap.put(object, result);
        }
        return resultMap;
    }

    public void clear() {
        // 删除所有位图
        List<String> keys = new ArrayList<String>();
        for (int i = 0; i <= getConfig().getBloomNumber(); i++) {
            keys.add(getBloomFilterKey(i));
        }
        for (String key : keys) {
            getJedisCluster().del(key);
        }
        // 删除配置
        String configKey = getConfig().getBloomFilterConfigKey();
        getJedisCluster().del(configKey);
    }

    /**
     * pipeline setbit
     */
    private boolean pipelineSetBit(String key, Set<Integer> offsetSet) {
        int slot = JedisClusterCRC16.getSlot(key);
        Jedis jedis = getJedisCluster().getConnectionFromSlot(slot);
        Pipeline pipeline = null;
        try {
            pipeline = jedis.pipelined();
            for (int offset : offsetSet) {
                pipeline.setbit(key, offset, true);
            }
            pipeline.sync();
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            if (pipeline != null)
                pipeline.clear();
            if (jedis != null)
                jedis.close();
        }
    }

    /**
     * pipeline get
     */
    private Map<Integer, Boolean> pipelineGetBit(String key, List<Integer> offsetList) {
        Map<Integer, Boolean> offsetResultMap = new HashMap<Integer, Boolean>();
        int slot = JedisClusterCRC16.getSlot(key);
        Jedis jedis = getJedisCluster().getConnectionFromSlot(slot);
        Pipeline pipeline = null;
        try {
            pipeline = jedis.pipelined();
            for (int offset : offsetList) {
                pipeline.getbit(key, offset);
            }
            List<Object> objectList = pipeline.syncAndReturnAll();
            int i = 0;
            for (Object object : objectList) {
                offsetResultMap.put(offsetList.get(i), (Boolean) object);
                i++;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (pipeline != null)
                pipeline.clear();
            if (jedis != null)
                jedis.close();
        }
        return offsetResultMap;
    }

    /**
     * 生成子布隆过滤器对应的key，使用crc16作为分组
     */
    private String genBloomFilterDistributeKey(T object) {
        int hashcode = JedisClusterCRC16.getCRC16(object.toString());
        int segement = hashcode % (getConfig().getBloomNumber() + 1);
        return getBloomFilterKey(segement);
    }

    /**
     * 获取布隆过滤器key
     */
    private String getBloomFilterKey(int index) {
        return getName() + ":" + index;
    }

    public long getExpectedInsertions() {
        return 0;
    }

    public double getFalseProbability() {
        return 0;
    }

    public long getSize() {
        return 0;
    }

    public int getHashIterations() {
        return 0;
    }

    private BloomFilterBuilder getConfig() {
        return config;
    }

    private HashFunction getHashFunction() {
        return getConfig().getHashFunction();
    }

    private List<Integer> hash(T object) {
        byte[] bytes = object.toString().getBytes();
        return getHashFunction().hash(bytes, getConfig().getBloomMaxSize(), getConfig().getHashIterations());
    }

    private JedisCluster getJedisCluster() {
        return getConfig().getJedisCluster();
    }

    private String getName() {
        return getConfig().getName();
    }
}
