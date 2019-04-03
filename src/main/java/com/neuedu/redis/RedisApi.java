package com.neuedu.redis;

import org.joda.time.Seconds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/*
* 封装redis的常用API
* */

@Component
@Configuration
public class RedisApi {
    @Autowired
    private JedisPool jedisPool;

/*
* set (key,value)
* */
/*
* @param key
* @param value
* @
* */
public String set (String key ,String value){
    String result = null;
    Jedis jedis = null;

    try {
        jedis = jedisPool.getResource();
        result = jedis.set(key,value);
    }catch (Exception e){
        jedisPool.returnResource(jedis);
    }finally {
        if (jedis !=null){
            jedisPool.returnResource(jedis);
        }
    }

    return result;

}

    /*
       *设置过期时间
     * @param key
     * @param value
     * @param second
     * */
    public String setex (String key ,int second,String value){
        String result = null;
        Jedis jedis = null;

        try {
            jedis = jedisPool.getResource();
            result = jedis.setex(key,second,value);
        }catch (Exception e){
            jedisPool.returnResource(jedis);
        }finally {
            if (jedis !=null){
                jedisPool.returnResource(jedis);
            }
        }

        return result;

    }


    /*
     *根据key 获取value
     * @param key
     * */
    public String get (String key ){
        String result = null;
        Jedis jedis = null;

        try {
            jedis = jedisPool.getResource();
            result = jedis.get(key);
        }catch (Exception e){
            jedisPool.returnResource(jedis);
        }finally {
            if (jedis !=null){
                jedisPool.returnResource(jedis);
            }
        }

        return result;

    }



    /*
     *删除
     * @param key
     * */
    public Long del (String key ){
        Long result = null;
        Jedis jedis = null;

        try {
            jedis = jedisPool.getResource();
            result = jedis.del(key);
        }catch (Exception e){
            jedisPool.returnResource(jedis);
        }finally {
            if (jedis !=null){
                jedisPool.returnResource(jedis);
            }
        }

        return result;

    }


    /**
     * 设置key的有效时间
     * */
    public Long set (String key,int second ){
        Long result = null;
        Jedis jedis = null;

        try {
            jedis = jedisPool.getResource();
            result = jedis.expire(key,second);
        }catch (Exception e){
            jedisPool.returnResource(jedis);
        }finally {
            if (jedis !=null){
                jedisPool.returnResource(jedis);
            }
        }

        return result;

    }


    /*
     *清除缓存
     * @param key
     * */
    public String flushCache (){
        String result = null;
        Jedis jedis = null;

        try {
            jedis = jedisPool.getResource();
            result = jedis.flushDB();
        }catch (Exception e){
            jedisPool.returnResource(jedis);
        }finally {
            if (jedis !=null){
                jedisPool.returnResource(jedis);
            }
        }

        return result;

    }

}
