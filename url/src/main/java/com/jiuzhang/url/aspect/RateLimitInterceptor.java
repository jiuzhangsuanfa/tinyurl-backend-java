package com.jiuzhang.url.aspect;

import com.google.common.util.concurrent.RateLimiter;
import com.jiuzhang.url.annotation.RateLimit;
import com.jiuzhang.url.enums.LimitType;
import com.jiuzhang.url.ratelimit.BucketTokenRateLimiter;
import com.jiuzhang.url.ratelimit.FixWindowRateLimiter;
import com.jiuzhang.url.ratelimit.RateLimitProcessor;
import com.jiuzhang.url.utils.IpUtil;
import com.jiuzhang.url.vo.RateLimiterInfo;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

@Aspect
@Configuration
public class RateLimitInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final RateLimitProcessor rateLimitProcessor;

    @Autowired
    public RateLimitInterceptor(RateLimitProcessor rateLimitProcessor) {
        this.rateLimitProcessor = rateLimitProcessor;
    }

    @Around("execution(public * *(..)) && @annotation(com.jiuzhang.url.annotation.RateLimit)")
    public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        if (method == null) {
            return null;
        }

        RateLimit rateLimitAnnotation = method.getAnnotation(RateLimit.class);
        LimitType limitType = rateLimitAnnotation.limitType();
        String key = getLimiterKey(request, method, rateLimitAnnotation, limitType);
        key = StringUtils.join(rateLimitAnnotation.prefix(), key);

        double permitsPerSecond = rateLimitAnnotation.permitsPerSecond();
        int period = rateLimitAnnotation.period();
        int permits = rateLimitAnnotation.permits();

        RateLimiterInfo rateLimiterInfo = new RateLimiterInfo();
        rateLimiterInfo.setKey(key);
        rateLimiterInfo.setPermitsPerSecond(permitsPerSecond);
        //RateLimiter rateLimiter = rateLimitProcessor.getRateLimiter(rateLimiterInfo); //rateLimitProcessor.getRateLimiter(key, permitsPerSecond);

        //FixWindowRateLimiter fixWindowRateLimiter = rateLimitProcessor.getFixWindowRateLimiter();
        BucketTokenRateLimiter bucketTokenRateLimiter = rateLimitProcessor.getBucketTokenRateLimiter();


        //if (RateLimitProcessor.isRateLimited(rateLimiter, period, permits)) {
        //if(fixWindowRateLimiter.isRateLimited(key, permits, period)) {
        if(bucketTokenRateLimiter.isRateLimited(key, permitsPerSecond, permits)) {
            logger.info("Access to {} from {} is rate limited", method.getName(), key);
            sendFallback();
            return null;
        }

        return pjp.proceed();
    }

    private String getLimiterKey(HttpServletRequest request, Method method, RateLimit rateLimitAnnotation, LimitType limitType) {
        String key = StringUtils.upperCase(method.getDeclaringClass().getSimpleName() + ":" + method.getName());
        switch (limitType) {
            case IP:
                key = key + "_" + LimitType.IP + ":" + IpUtil.getIpAddr(request);
                break;
            case CUSTOMER:
                key = key + "_" + rateLimitAnnotation.key();
                break;
        }
        return key;
    }

    private void sendFallback() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = requestAttributes.getResponse();
        response.setContentType("text/html;charset=UTF-8");
        response.setStatus(429);
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.println("Too Many Requests");
        } catch (IOException e) {
            logger.error("Write fall back failed", e);
        } finally {
            writer.close();
        }
    }
}
