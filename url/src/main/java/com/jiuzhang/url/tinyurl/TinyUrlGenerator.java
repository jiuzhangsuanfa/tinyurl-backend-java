package com.jiuzhang.url.tinyurl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import com.jiuzhang.url.config.LocalCacheProperties;
import com.jiuzhang.url.ratelimit.BucketTokenRateLimiter;
import com.jiuzhang.url.ratelimit.FixWindowRateLimiter;
import com.jiuzhang.url.vo.RateLimiterInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class TinyUrlGenerator {

    private static final int DEFAULT_URL_LENGTH = 6;

    public String generate(){
        return RandomTinyUrl.generate(DEFAULT_URL_LENGTH);
    }


    public String generate(long id){
        return Base62TinyUrl.generate(id, DEFAULT_URL_LENGTH);
    }

    public long convertTinyUrlToId(String url){
        return Base62TinyUrl.tinyUrlToId(url);
    }
}
