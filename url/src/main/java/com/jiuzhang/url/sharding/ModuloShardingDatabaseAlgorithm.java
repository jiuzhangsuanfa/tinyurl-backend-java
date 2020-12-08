package com.jiuzhang.url.sharding;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;
import java.util.Optional;

@Slf4j
public class ModuloShardingDatabaseAlgorithm implements PreciseShardingAlgorithm<String> {

  @Override
  public String doSharding(
      Collection<String> databaseNames, PreciseShardingValue<String> shardingValue) {

    log.info("databaseNames:{}", JSON.toJSONString(databaseNames));
    log.info("shardingValue:{}", JSON.toJSONString(shardingValue));

    String databaseName = "";

    if ("LONG_TO_SHORT".equalsIgnoreCase(shardingValue.getLogicTableName())
        || "LONG_TO_SEQUENCE_ID".equalsIgnoreCase(shardingValue.getLogicTableName())) {

      String shardValue = Optional.of(shardingValue.getValue()).orElse("");

      if (StringUtils.isNotBlank(shardValue)) {
        databaseName =
            findDatabaseName(shardValue.hashCode() % databaseNames.size() + "", databaseNames);
      }
    }

    if ("SHORT_TO_LONG".equalsIgnoreCase(shardingValue.getLogicTableName())) {
      String dbIndexStr = StringUtils.left(shardingValue.getValue(), 1);
      if (StringUtils.isNotBlank(dbIndexStr)) {
        databaseName = findDatabaseName(dbIndexStr, databaseNames);
      }
    }

    log.info("databaseName:{}", databaseName);
    if (StringUtils.isNotEmpty(databaseName)) {
      return databaseName;
    }

    throw new UnsupportedOperationException();
  }

  private String findDatabaseName(String dbIndexStr, Collection<String> databaseNames) {
    String databaseName = "";

    for (String each : databaseNames) {
      if (each.endsWith(dbIndexStr)) {
        databaseName = each;
        break;
      }
    }

    return databaseName;
  }

}
