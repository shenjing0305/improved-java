package com.example.springbootredis;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;

import java.util.LinkedHashSet;
import java.util.Set;


@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //redis哨兵模式配置
    @Value("${spring.redis.sentinel.nodes:}")
    private String sentinel;
    @Value("${spring.redis.sentinel.master:}")
    private String master;

    //redis单机模式配置
    @Value("${spring.redis.host:}")
    private String host;
    @Value("${spring.redis.port:}")
    private Integer port;

    //redis集群模式配置
    @Value("${spring.redis.nodes:}")
    private String nodes;

    @Value("${spring.redis.password:}")
    private String password;
    @Value("${spring.redis.database:0}")
    private Integer database;
    @Value("${spring.redis.max-redirects:3}")
    private Integer maxRedirects;
    @Value("${spring.redis.timeout:}")
    private String timeout;
    @Value("${spring.redis.jedis.pool.max-active:8}")
    private Integer maxActive;
    @Value("${spring.redis.jedis.pool.max-idle:8}")
    private Integer maxIdle;
    @Value("${spring.redis.jedis.pool.max-wait:-1}")
    private Long maxWait;
    @Value("${spring.redis.jedis.pool.min-idle:0}")
    private Integer minIdle;


    /**
     * 连接池配置信息
     * @return
     */
    @Bean
    public JedisPoolConfig getJedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWait);
        jedisPoolConfig.setMaxTotal(maxActive);
        jedisPoolConfig.setMinIdle(minIdle);
        return jedisPoolConfig;
    }

    @Bean
    @ConditionalOnMissingBean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = null;
        JedisClientConfiguration.JedisPoolingClientConfigurationBuilder jpcb =
                (JedisClientConfiguration.JedisPoolingClientConfigurationBuilder) JedisClientConfiguration.builder();
        jpcb.poolConfig(getJedisPoolConfig());
        JedisClientConfiguration jedisClientConfiguration = jpcb.build();

        //如果是哨兵的模式
        if (StringUtils.isNotBlank(sentinel) && StringUtils.isNotBlank(master)) {
            logger.info("->->->Redis use SentinelConfiguration");
            RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration();
            String[] sentinelArray = sentinel.split(",");
            for (String s : sentinelArray) {
                try {
                    String[] split1 = s.split(":");
                    redisSentinelConfiguration.addSentinel(new RedisNode(split1[0], Integer.parseInt(split1[1])));
                } catch (Exception e) {
                    throw new RuntimeException(String.format("出现配置错误!请确认sentinelArray=[%s]是否正确", nodes));
                }
            }
            redisSentinelConfiguration.setMaster(master);
            redisSentinelConfiguration.setPassword(RedisPassword.of(password));
            redisSentinelConfiguration.setDatabase(database);
            factory = new JedisConnectionFactory(redisSentinelConfiguration, jedisClientConfiguration);

        }
        //单机模式
        else if (StringUtils.isNotBlank(host)) {
            logger.info("->->->Redis use RedisStandaloneConfiguration");
            RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
            if (!StringUtils.isEmpty(password)) {
                redisStandaloneConfiguration.setPassword(RedisPassword.of(password));
            }
            redisStandaloneConfiguration.setPort(port);
            redisStandaloneConfiguration.setHostName(host);
            redisStandaloneConfiguration.setDatabase(database);
            factory = new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration);
        }
        //集群配置信息实现
        else {
            logger.info("->->->Redis use RedisClusterConfiguration");
            String[] split = nodes.split(",");
            Set<HostAndPort> nodes = new LinkedHashSet<>();
            for (int i = 0; i < split.length; i++) {
                try {
                    String[] split1 = split[i].split(":");
                    nodes.add(new HostAndPort(split1[0], Integer.parseInt(split1[1])));
                } catch (Exception e) {
                    throw new RuntimeException(String.format("出现配置错误!请确认node=[%s]是否正确", nodes));
                }
            }
            RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
            nodes.forEach(n -> {
                redisClusterConfiguration.addClusterNode(new RedisNode(n.getHost(), n.getPort()));
            });
            if (!StringUtils.isEmpty(password)) {
                redisClusterConfiguration.setPassword(RedisPassword.of(password));
            }
            redisClusterConfiguration.setMaxRedirects(maxRedirects);
            factory = new JedisConnectionFactory(redisClusterConfiguration, jedisClientConfiguration);
        }
        return factory;
    }



    @Bean
    public RedisTemplate<String, Object> redisTemplate(JedisConnectionFactory jedisConnectionFactory){
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory);
        Jackson2JsonRedisSerializer jacksonSeial = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        jacksonSeial.setObjectMapper(om);
        StringRedisSerializer stringSerial = new StringRedisSerializer();
        template.setKeySerializer(stringSerial);
        template.setValueSerializer(stringSerial);
        template.setHashKeySerializer(stringSerial);
        template.setHashValueSerializer(stringSerial);
        return template;
    }
}
