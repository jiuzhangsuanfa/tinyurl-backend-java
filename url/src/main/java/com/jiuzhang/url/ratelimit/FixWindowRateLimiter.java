package com.jiuzhang.url.ratelimit;

import com.jiuzhang.url.aspect.RateLimitInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;


import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

@Component
public class FixWindowRateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(FixWindowRateLimiter.class);

    //Extend the expiration time by a few seconds to avoid misses.
    private static  final int EXPIRATION_FUDGE = 5;
    private final RedisTemplate<String, Serializable> limitRedisTemplate;

    @Autowired
    public FixWindowRateLimiter(RedisTemplate<String, Serializable> limitRedisTemplate) {
        this.limitRedisTemplate = limitRedisTemplate;
    }

    public  boolean isRateLimited(String key, int limit, int period){
        long window = getWindow(key, period);
        String cacheKey = makeCacheKey(window, key, limit, period);

        Long count = limitRedisTemplate.opsForValue().increment(cacheKey, 1);
        if (count != null && count == 1) {
            limitRedisTemplate.expire(cacheKey, period + EXPIRATION_FUDGE, TimeUnit.SECONDS);
        }

        //防止出现并发操作未设置超时时间的场景
        if (limitRedisTemplate.getExpire(cacheKey, TimeUnit.SECONDS) == -1) {
            limitRedisTemplate.expire(cacheKey, period + EXPIRATION_FUDGE, TimeUnit.SECONDS);
        }

        return count > limit;
    }


    private static String makeCacheKey(long window, String key, int limit, int period){
        String keyStr = StringUtils.join(limit, period, key, window);
        return DigestUtils.md5Hex(keyStr);
    }

    private static long getWindow(String key, int period) {
        long timeStamp = getCurrentTimeStamp();

        if(period == 1) {
            return timeStamp;
        }

        byte[] keyInBytes = null;
        try {
            keyInBytes = StringUtils.isNotBlank(key) ?  key.getBytes("utf-8") : null;
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to decode key to utf-8", e);
        }

        long staggeredWindow = keyInBytes != null ? getStaggeredWindow(keyInBytes, period):0l;
        long window = timeStamp - (timeStamp % period) + staggeredWindow;

        return window < timeStamp ? window + period : window;
    }

    private static long getStaggeredWindow(byte[] keyInBytes, int period){
        CRC32 crc32 = new CRC32();
        crc32.update(keyInBytes);

        return crc32.getValue() % period;
    }

    private static long getCurrentTimeStamp(){
        Instant instant = Instant.now();
        long timeStampSeconds = instant.getEpochSecond();

        return timeStampSeconds;
    }
}
