package com.jiuzhang.url.dbconfig;


import com.jiuzhang.url.enums.DBShard;
import com.jiuzhang.url.sharding.ModuloShardingDatabaseAlgorithm;
import com.jiuzhang.url.utils.DataSourceUtil;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
//@AutoConfigureAfter(DBMigrationConfig.class)
public class DataSourceConfig {

    @Resource
    RoutingDataSource routingDataSource;

    @Bean
    @DependsOn(value = "flywayMigrationInitializer")
    public DataSource userShardingDataSource() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();

        TableRuleConfiguration longToShortShardingRule = new TableRuleConfiguration("LONG_TO_SHORT", "url${0..1}.LONG_TO_SHORT");
        longToShortShardingRule.setDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("longUrl",
                new ModuloShardingDatabaseAlgorithm()));

        TableRuleConfiguration longToSequenceIdShardingRule = new TableRuleConfiguration("LONG_TO_SEQUENCE_ID", "url${0..1}.LONG_TO_SEQUENCE_ID");
        longToSequenceIdShardingRule.setDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("longUrl",
                new ModuloShardingDatabaseAlgorithm()));

        TableRuleConfiguration shortToLongShardingRule = new TableRuleConfiguration("SHORT_TO_LONG", "url${0..1}.SHORT_TO_LONG");
        shortToLongShardingRule.setDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("shortUrl",
                new ModuloShardingDatabaseAlgorithm()));

        shardingRuleConfig.getTableRuleConfigs().add(longToShortShardingRule);
        shardingRuleConfig.getTableRuleConfigs().add(longToSequenceIdShardingRule);
        shardingRuleConfig.getTableRuleConfigs().add(shortToLongShardingRule);


        Map<String, DataSource> dbMap = new HashMap<>(2);
        //dbMap.put(DBShard.url0.name(), DataSourceUtil.createDataSource(DBShard.url0.name(), 3307));
        //dbMap.put(DBShard.url1.name(), DataSourceUtil.createDataSource(DBShard.url1.name(), 3308));
        dbMap.put(DBShard.url0.name(), routingDataSource.getDataSourceByShard(DBShard.url0));
        dbMap.put(DBShard.url1.name(), routingDataSource.getDataSourceByShard(DBShard.url1));

        Properties properties = new Properties();
        properties.put("sql.show", true);

        return ShardingDataSourceFactory.createDataSource(dbMap, shardingRuleConfig, properties);
    }
}
