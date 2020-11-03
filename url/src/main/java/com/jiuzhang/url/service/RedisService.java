package com.jiuzhang.url.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private RedisTemplate dbCacheRedisTemplate;

    @Autowired
    public RedisService(RedisTemplate dbCacheRedisTemplate) {
        this.dbCacheRedisTemplate = dbCacheRedisTemplate;
    }

    public void setLongAndShort(String longUrl, String shortUrl, long time) {
        dbCacheRedisTemplate.opsForValue().set(longUrl, shortUrl, time, TimeUnit.MINUTES);
        dbCacheRedisTemplate.opsForValue().set(shortUrl, longUrl, time, TimeUnit.MINUTES);
        //redisTemplate.opsForValue().set(shortUrl + "sum", 0, 60, TimeUnit.MINUTES);
    }

    public void expire(String key, long time) {
        dbCacheRedisTemplate.expire(key, time, TimeUnit.MINUTES);
    }

    public void set(String key, String value) {
        dbCacheRedisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, long time) {
        if (time > 0) {
            dbCacheRedisTemplate.opsForValue().set(key, value, time, TimeUnit.MINUTES);
        } else {
            dbCacheRedisTemplate.opsForValue().set(key, value);
        }
    }


    public Object get(String key) {
        return key == null ? null : dbCacheRedisTemplate.opsForValue().get(key);
    }
}
