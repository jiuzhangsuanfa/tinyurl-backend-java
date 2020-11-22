package com.jiuzhang.url.dbconfig;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

@Getter
public class Shard2DBProperties extends DBProperties {
    @Value("${shard2.datasource.url}")
    private String url;

    @Value("${shard2.datasource.username}")
    private String username;

    @Value("${shard2.datasource.password}")
    private String password;
}
