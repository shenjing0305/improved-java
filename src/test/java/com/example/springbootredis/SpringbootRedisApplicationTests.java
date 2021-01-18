package com.example.springbootredis;

import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, RedisAutoConfiguration.class, RedissonAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class})
@SpringBootTest
class SpringbootRedisApplicationTests {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;


    @Test
    void contextLoads() {
    }

    @Test
    void testRedis(){
        try {
            redisTemplate.opsForValue().set("key00999", "2222");
            RLock rLock = redissonClient.getLock("lock1111");
            logger.info("======获取到锁了。。。。"+rLock.getName());

        }catch (Exception e){
            logger.error("error happened: " + e.getMessage(), e);
        }

    }

}
