package com.jiuzhang.url.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @auther: WZ
 * @Date: 2020/9/9 15:28
 * @Description: 线程池配置信息存放
 */
@ConfigurationProperties(prefix = "tinyurl.thread")
@Data
public class ThreadPooProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;

}
