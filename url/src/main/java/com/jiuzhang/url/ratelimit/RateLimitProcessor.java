package com.jiuzhang.url.ratelimit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import com.jiuzhang.url.config.LocalCacheConfigProperties;
import com.jiuzhang.url.vo.RateLimiterInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitProcessor {

    private final LocalCacheConfigProperties localCacheConfigProperties;
    private LoadingCache<RateLimiterInfo, RateLimiter> rateLimiterCache;
    private ConcurrentHashMap<String, RateLimiter> rateLimiters;

    @Autowired
    public RateLimitProcessor(LocalCacheConfigProperties localCacheConfigProperties) {
        this.localCacheConfigProperties = localCacheConfigProperties;
    }

    public static boolean isRateLimited(RateLimiter rateLimiter, int period, int permits) {
        boolean tryAcquire = rateLimiter.tryAcquire(permits, period, TimeUnit.SECONDS);
        return !tryAcquire;
    }

    @PostConstruct
    private void createRateLimiterCache() {
        rateLimiters = new ConcurrentHashMap();
        rateLimiterCache =
                CacheBuilder.newBuilder()
                        .expireAfterAccess(localCacheConfigProperties.getKeepAliveTime(), TimeUnit.MINUTES)
                        .build(new CacheLoader<RateLimiterInfo, RateLimiter>() {

                            @Override
                            public RateLimiter load(RateLimiterInfo rateLimiterInfo) throws Exception {
                                return createRateLimiter(rateLimiterInfo);
                            }
                        });
    }

    private RateLimiter createRateLimiter(RateLimiterInfo rateLimiterInfo) {
        RateLimiter rateLimiter = RateLimiter.create(rateLimiterInfo.getPermitsPerSecond());
        return rateLimiter;
    }

    public RateLimiter getRateLimiter(RateLimiterInfo key) {
        RateLimiter rateLimiter = rateLimiterCache.getUnchecked(key);
        return rateLimiter;
    }

    public RateLimiter getRateLimiter(String key, double permitsPerSecond) {
        RateLimiter rateLimiter = rateLimiters.get(key);
        if (rateLimiter == null) {
            rateLimiter = RateLimiter.create(permitsPerSecond);
            RateLimiter rateLimiterPrevious = rateLimiters.putIfAbsent(key, rateLimiter);
            if (rateLimiterPrevious != null) {
                rateLimiter = rateLimiterPrevious;
            }
        }

        return rateLimiter;
    }

}
