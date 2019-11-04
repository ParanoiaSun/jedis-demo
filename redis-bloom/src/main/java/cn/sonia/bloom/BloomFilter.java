package cn.sonia.bloom;

import java.util.List;
import java.util.Map;

/**
 * Bloom过滤器接口
 * Create By Sonia_Sun on 2019-11-04
 */
public interface BloomFilter<T> {

    /**
     * 添加
     * @param object 对象
     * @return 是否添加成功
     */
    boolean add(T object);

    /**
     * 批量添加
     * @param objectList 对象列表
     * @return 是否添加成功
     */
    boolean batchAdd(List<T> objectList);

    /**
     * 是否包含
     * @param object 对象
     */
    boolean contains(T object);

    /**
     * 是否包含
     * @param objectList 对象列表
     */
    Map<T, Boolean> batchContains(List<T> objectList);

    /**
     * 删除
     */
    void clear();

    /**
     * 预期插入数量
     */
    long getExpectedInsertions();

    /**
     * 预期错误概率
     */
    double getFalseProbability();

    /**
     * 布隆过滤器总长度
     */
    long getSize();

    /**
     * hash函数迭代次数
     */
    int getHashIterations();

}
