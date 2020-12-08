package com.jiuzhang.url.dbconfig;

import com.jiuzhang.url.enums.DBShard;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DBMigrationConfig {
    @Bean
    public Shard1DBProperties shard1DBProperties() {
        return new Shard1DBProperties();
    }

    @Bean
    public Shard2DBProperties shard2DBProperties() {
        return new Shard2DBProperties();
    }

    @Bean
    public RoutingDataSource routingDataSource() {
        Map<DBShard, DBProperties> configurations = new HashMap<>();
        configurations.put(DBShard.url0, shard1DBProperties());
        configurations.put(DBShard.url1, shard2DBProperties());

        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.initDataSources(configurations);
        return routingDataSource;
    }

    @Bean(initMethod = "migrate")
    @DependsOn(value ="routingDataSource")
    public FlywayMigrationInitializer flywayMigrationInitializer() {
        return new FlywayMigrationInitializer();
    }
}
