package com.jiuzhang.url.aspect;

import com.google.common.util.concurrent.RateLimiter;
import com.jiuzhang.url.annotation.TinyUrlRateLimiter;
import com.jiuzhang.url.common.LimitType;
import com.jiuzhang.url.utils.IpUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@Aspect
@Configuration
public class LimitInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LimitInterceptor.class);

    private ConcurrentHashMap<String, RateLimiter> rateLimiters;

    public LimitInterceptor() {
        rateLimiters = new ConcurrentHashMap();
    }

    @Around("execution(public * *(..)) && @annotation(com.jiuzhang.url.annotation.TinyUrlRateLimiter)")
    public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        if (method == null) {
            return null;
        }

        TinyUrlRateLimiter tinyUrlRateLimiterAnnotation = method.getAnnotation(TinyUrlRateLimiter.class);
        LimitType limitType = tinyUrlRateLimiterAnnotation.limitType();
        String key =  getLimiterKey(request, method, tinyUrlRateLimiterAnnotation, limitType);
        key = StringUtils.join(tinyUrlRateLimiterAnnotation.prefix(), key);

        double permitsPerSecond = tinyUrlRateLimiterAnnotation.permitsPerSecond();
        int period = tinyUrlRateLimiterAnnotation.period();
        int permits = tinyUrlRateLimiterAnnotation.permits();

        RateLimiter rateLimiter =  getRateLimiter(key, permitsPerSecond);

        if(isRateLimited(rateLimiter, period, permits)) {
            logger.info("Access to {} from {} is rate limited", method.getName(), key);
            sendFallback();
            return null;
        }

        return pjp.proceed();

    }

    private String getLimiterKey(HttpServletRequest request, Method method, TinyUrlRateLimiter tinyUrlRateLimiterAnnotation, LimitType limitType) {
        String key;
        switch (limitType) {
            case IP:
                key = IpUtil.getIpAddr(request);
                break;
            case CUSTOMER:
                key = tinyUrlRateLimiterAnnotation.key();
                break;
            default:
                key = StringUtils.upperCase(method.getName());
        }
        return key;
    }

    private RateLimiter getRateLimiter(String key, double permitsPerSecond) {
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

    private boolean isRateLimited(RateLimiter rateLimiter, int period, int permits){
        boolean tryAcquire = rateLimiter.tryAcquire(permits, period, TimeUnit.MILLISECONDS);
        return !tryAcquire;
    }

    private void sendFallback() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = requestAttributes.getResponse();
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.println("Service is busy, please try again later");
        } catch (IOException e) {
            logger.error("Write fall back failed", e);
        } finally {
            writer.close();
        }
    }
}
