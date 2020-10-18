package com.jiuzhang.url.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tinyurl.local.cache")
@Data
public class LocalCacheConfigProperties {
    private Integer keepAliveTime;
}
