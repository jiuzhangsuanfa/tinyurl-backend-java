package com.jiuzhang.url.dbconfig;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

@Getter
public class Shard1DBProperties extends DBProperties {

    @Value("${shard1.datasource.url}")
    private String url;

    @Value("${shard1.datasource.username}")
    private String username;

    @Value("${shard1.datasource.password}")
    private String password;

}
