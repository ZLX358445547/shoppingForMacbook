package com.neuedu.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
/*
*springIoc 的配置方式
*1.基于xml文件配置
*2.基于注解注释
*3.基于java容器的配置
* 配置连接池
* */





@Component
@Configuration
public class RedisPool {

    @Autowired
    RedisProperties redisProperties;
    @Bean
   public JedisPool jedisPool(){
       JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

       //设置最大连接数
       jedisPoolConfig.setMaxTotal(redisProperties.getMaxTotal());

       //最大空闲数
       jedisPoolConfig.setMaxIdle(redisProperties.getMaxIdle());

       //最小空闲数
        jedisPoolConfig.setMinIdle(redisProperties.getMinIdle());

       //在获取实例时，校验实例是否有效，借
        jedisPoolConfig.setTestOnBorrow(redisProperties.isTestBorrow());
        //返还
      jedisPoolConfig.setTestOnReturn(redisProperties.isTestReturn());



       //当连接池中的连接消耗完毕，true：等待连接，false：抛出异常
       jedisPoolConfig.setBlockWhenExhausted(true);

       return new JedisPool(jedisPoolConfig,redisProperties.getRedisIp(),redisProperties.getRedisPort(),2000);
   }

    public static void main(String[] args) {
        /*JedisPool jedisPool = new RedisPool().jedisPool();
        Jedis jedis = jedisPool.getResource();
        jedis = jedisPool.getResource();
        String  value = jedis.set("zlx","zhanglixing");
        //将连接还回去
        jedisPool.returnResource(jedis);*/
        Jedis jedis = new Jedis("39.96.66.122",6379);
        jedis.set("aaa","bbb");



    }

}
