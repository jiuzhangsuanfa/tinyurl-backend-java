package com.jiuzhang.url.service;

import com.jiuzhang.url.sharding.RedisKeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Service
public class SequenceIdService {

  private static final String SEQUENCE_ID = "Sequence_ID";

  private static final String GLOBAL_SEQUENCE_ID = "Global_Sequence_ID";

  private final RedisTemplate sequenceRedisTemplate;

  private final RedisKeyGenerator redisKeyGenerator;

  private RedisAtomicLong entityIdCounter;

  private DefaultRedisScript<Number> redisScript;

  @Autowired
  public SequenceIdService(RedisTemplate sequenceRedisTemplate, RedisKeyGenerator redisKeyGenerator) {
    this.sequenceRedisTemplate = sequenceRedisTemplate;
    this.redisKeyGenerator = redisKeyGenerator;
  }

  @PostConstruct
  private void getRedisScript() {
    entityIdCounter =
        new RedisAtomicLong(SEQUENCE_ID, sequenceRedisTemplate.getConnectionFactory());

    redisScript = new DefaultRedisScript<>();
    redisScript.setScriptSource(
        new ResourceScriptSource(new ClassPathResource("redisCounter.lua")));
    redisScript.setResultType(Number.class);
  }

  public long getNextSequenceByAtomic() {
    Long increment = entityIdCounter.getAndIncrement();
    sequenceRedisTemplate.getConnectionFactory().getConnection().bgSave();
    return increment;
  }

  public long getNextSequenceByLua() {
    List<String> keys = Arrays.asList(GLOBAL_SEQUENCE_ID);
    Number count = (Number) sequenceRedisTemplate.execute(redisScript, keys);
    sequenceRedisTemplate.getConnectionFactory().getConnection().bgSave();
    return count.longValue();
  }

  public long getNextSequenceByKeyGenerator() {
    Number count = this.redisKeyGenerator.generateKey();
    return count.longValue();
  }

}
