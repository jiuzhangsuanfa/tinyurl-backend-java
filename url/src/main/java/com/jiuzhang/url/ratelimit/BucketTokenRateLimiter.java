package com.jiuzhang.url.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class BucketTokenRateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(BucketTokenRateLimiter.class);

    private final RedisTemplate<String, Serializable> limitRedisTemplate;

    private DefaultRedisScript<Number> redisScript;

    @Autowired
    public BucketTokenRateLimiter(RedisTemplate<String, Serializable> limitRedisTemplate) {
        this.limitRedisTemplate = limitRedisTemplate;
    }

    @PostConstruct
    private void getRedisScript() {
        redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redisLimit.lua")));
        redisScript.setResultType(Number.class);
    }

    public boolean isRateLimited(String key, double permitsPerSecond, int limit) {
        List<String> keys = getKeys(key);

        Number count = limitRedisTemplate.execute(
                redisScript,
                keys,
                permitsPerSecond,
                limit,
                getCurrentTimeStamp(),
                1);

        return count.intValue() != 1;
    }

    static List<String> getKeys(String id) {
        String prefix = "rate_limiter.{" + id;
        String tokenKey = prefix + "}.tokens";
        String timestampKey = prefix + "}.timestamp";
        return Arrays.asList(tokenKey, timestampKey);
    }


    private static long getCurrentTimeStamp() {
        Instant instant = Instant.now();
        long timeStampSeconds = instant.getEpochSecond();

        return timeStampSeconds;
    }
}
