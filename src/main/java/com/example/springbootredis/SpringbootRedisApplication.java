package com.example.springbootredis;

import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, RedisAutoConfiguration.class, RedissonAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class})
@SpringBootApplication
public class SpringbootRedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootRedisApplication.class, args);
    }



}
