package com.jiuzhang.url.ratelimit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import com.jiuzhang.url.config.LocalCacheProperties;
import com.jiuzhang.url.vo.RateLimiterInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitProcessor {

    private final LocalCacheProperties localCacheProperties;
    private LoadingCache<RateLimiterInfo, RateLimiter> rateLimiterCache;
    private ConcurrentHashMap<String, RateLimiter> rateLimiters;
    private final FixWindowRateLimiter fixWindowRateLimiter;
    private final BucketTokenRateLimiter bucketTokenRateLimiter;

    @Autowired
    public RateLimitProcessor(LocalCacheProperties localCacheProperties, FixWindowRateLimiter fixWindowRateLimiter, BucketTokenRateLimiter bucketTokenRateLimiter) {
        this.localCacheProperties = localCacheProperties;
        this.fixWindowRateLimiter = fixWindowRateLimiter;
        this.bucketTokenRateLimiter = bucketTokenRateLimiter;
    }

    public static boolean isRateLimited(RateLimiter rateLimiter, int period, int permits) {
        boolean tryAcquire = rateLimiter.tryAcquire(permits, period, TimeUnit.SECONDS);
        return !tryAcquire;
    }

    public static boolean isRateLimited(FixWindowRateLimiter fixWindowRateLimiter, String key, int limit, int period) {
        return fixWindowRateLimiter.isRateLimited(key, limit, period);
    }

    public static boolean isRateLimited(BucketTokenRateLimiter bucketTokenRateLimiter, String key, double permitsPerSecond, int limit) {
        return bucketTokenRateLimiter.isRateLimited(key, permitsPerSecond, limit);
    }

    @PostConstruct
    private void createRateLimiterCache() {
        rateLimiters = new ConcurrentHashMap();
        rateLimiterCache =
                CacheBuilder.newBuilder()
                        .expireAfterAccess(localCacheProperties.getKeepAliveTime(), TimeUnit.MINUTES)
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

    public FixWindowRateLimiter getFixWindowRateLimiter() {
        return fixWindowRateLimiter;
    }

    public BucketTokenRateLimiter getBucketTokenRateLimiter() {
        return bucketTokenRateLimiter;
    }
}
