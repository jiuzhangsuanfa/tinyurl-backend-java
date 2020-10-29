package com.jiuzhang.url.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tinyurl.redis.db")
@Data
public class RedisDBProperties {
    private String host;
    private Integer port;
}
