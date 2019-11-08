package sn.sonia.redis;

import cn.sonia.redis.service.RedisService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * Create By Sonia_Sun on 2019-11-08
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:springContext.xml")
public class RedisClusterSpringTest {

    @Resource(name = "redisClusterService")
    private RedisService redisClusterService;

    @Test
    public void testNotNull() {
        assertNotNull(redisClusterService);
        redisClusterService.set("hello", "world");
        assertEquals("world", redisClusterService.get("hello"));
    }
}
