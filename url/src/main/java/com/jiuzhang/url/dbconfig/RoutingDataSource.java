package com.jiuzhang.url.dbconfig;

import com.jiuzhang.url.enums.DBShard;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class RoutingDataSource  extends AbstractRoutingDataSource {

    private static final Map<Object, Object> dataSourceMap = new HashMap<>();

    @Override
    protected Object determineCurrentLookupKey() {
        return null;
    }

    void initDataSources(Map<DBShard, DBProperties> configurations) {
        for (DBShard shard : DBShard.values()) {
            dataSourceMap.put(shard, new HikariDataSource(hikariConfig(configurations.get(shard))));
        }
        setDefaultTargetDataSource(getDefaultDataSource());
        setTargetDataSources(dataSourceMap);
    }

    DataSource getDataSourceByShard(DBShard shard) {
        return (DataSource) dataSourceMap.get(shard);
    }

    DataSource getDefaultDataSource() {
        return getDataSourceByShard(DBShard.url0);
    }

  private HikariConfig hikariConfig(DBProperties configuration) {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
    hikariConfig.setJdbcUrl(configuration.getUrl());
    hikariConfig.setUsername(configuration.getUsername());
    hikariConfig.setPassword(configuration.getPassword());

    return hikariConfig;
  }

}
