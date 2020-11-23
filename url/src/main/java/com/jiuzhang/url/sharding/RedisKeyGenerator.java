package com.jiuzhang.url.sharding;

import com.jiuzhang.url.config.RedisDBProperties;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.support.ConnectionPoolSupport;
import io.shardingjdbc.core.keygen.KeyGenerator;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisKeyGenerator implements KeyGenerator {

  private final RedisDBProperties redisDBProperties;

  private RedisClient client = null;
  private GenericObjectPool<StatefulRedisConnection<String, String>> pool = null;

  @Autowired
  public RedisKeyGenerator(RedisDBProperties redisDBProperties) {
      this.redisDBProperties = redisDBProperties;
    this.client = createRedisClient();
    this.pool =
        ConnectionPoolSupport.createGenericObjectPool(
            () -> client.connect(), getGenericObjectPoolConfig());
  }

  private RedisClient createRedisClient() {
    RedisClient client =
        RedisClient.create(
            RedisURI.Builder.redis(
                    this.redisDBProperties.getHost(), this.redisDBProperties.getPort())
                .withTimeout(Duration.ofMillis(6000))
                .withDatabase(1)
                .build());
    return client;
  }

  private GenericObjectPoolConfig getGenericObjectPoolConfig() {
    GenericObjectPoolConfig config = new GenericObjectPoolConfig();
    config.setMaxIdle(8);
    config.setMinIdle(0);
    config.setMaxTotal(8);
    config.setMaxWaitMillis(-1);
    return config;
  }

  @Override
  public synchronized Number generateKey() {
    try (StatefulRedisConnection<String, String> connection = pool.borrowObject()) {
      RedisAsyncCommands<String, String> commands = connection.async();
      RedisFuture<Long> future = commands.incr("id");
      return future.get();
    } catch (Exception e) {
      e.printStackTrace();
      return System.currentTimeMillis();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    if (!pool.isClosed()) {
      pool.close();
    }
    super.finalize();
  }

}
