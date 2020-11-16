package com.jiuzhang.url.service;

import com.jiuzhang.url.domain.LongToSequenceId;
import com.jiuzhang.url.domain.LongToShort;
import com.jiuzhang.url.repo.LongToSequenceIdRepository;
import com.jiuzhang.url.repo.LongToShortRepository;
import com.jiuzhang.url.tinyurl.TinyUrlGenerator;
import com.jiuzhang.url.utils.UrlUtil;
import com.jiuzhang.url.vo.UrlVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.BiFunction;

@Service
public class LongToShortService implements ILongToShortService {

  private static final Logger logger = LoggerFactory.getLogger(LongToShortService.class);
  private static final int DEFAULT_CACHE_TTL = 60;
  private final LongToShortRepository longToShortRepository;
  private final LongToSequenceIdRepository longToSequenceIdRepository;
  private final RedisService redisService;
  private final SequenceIdService sequenceIdService;
  private final ThreadPoolExecutor executor;
  private final TinyUrlGenerator tinyUrlGenerator;

  @Value("${shorturl.prefix}")
  private String shortUrlPrefix;

  @Autowired
  public LongToShortService(
      LongToShortRepository longToShortRepository,
      LongToSequenceIdRepository longToSequenceIdRepository,
      RedisService redisService,
      SequenceIdService sequenceIdService,
      ThreadPoolExecutor executor,
      TinyUrlGenerator tinyUrlGenerator) {
    this.longToShortRepository = longToShortRepository;
    this.longToSequenceIdRepository = longToSequenceIdRepository;
    this.redisService = redisService;
    this.sequenceIdService = sequenceIdService;
    this.executor = executor;
    this.tinyUrlGenerator = tinyUrlGenerator;
  }

  /**
   * @param longUrl
   * @param request
   * @return
   */
  @Override
  @Transactional
  public UrlVO longToShort(String longUrl, HttpServletRequest request) {
    if (!UrlUtil.isValidLongUrl(longUrl)) {
      logger.error("Invalid long URL");
      return null;
    }

    UrlVO urlVo = fetchTinyUrlFromCache(longUrl);
    if (urlVo != null) {
      return urlVo;
    }

    Optional<LongToShort> longToShortOpt = longToShortRepository.findByLongUrl(longUrl);
    if (longToShortOpt.isPresent()) {
      return postProcessDataFromDB(longToShortOpt.get());
    }

    String shortUrl = fetchNextAvailableShortUrl();

    redisService.setLongAndShort(longUrl, shortUrl, DEFAULT_CACHE_TTL);

    persistLongToShortAsync(longUrl, shortUrl, this::persistLongToShort);

    urlVo = createUrlVO(shortUrl);

    return urlVo;
  }

  @Override
  public UrlVO longToShort(String longUrl) {
    if (!UrlUtil.isValidLongUrl(longUrl)) {
      logger.error("Invalid long URL");
      return null;
    }

    String sequenceIdStr = fetchValueByKey(longUrl);
    if (NumberUtils.isDigits(sequenceIdStr)) {
      Long sequenceId = NumberUtils.createLong(sequenceIdStr);
      if (sequenceId != null) {
        return convertSequenceIdToShortKey(sequenceId);
      }
    }

    if (StringUtils.isNotBlank(sequenceIdStr)) {
      return createUrlVO(sequenceIdStr);
    }

    Optional<LongToSequenceId> longToSequenceIdOpt =
        longToSequenceIdRepository.findByLongUrl(longUrl);
    if (longToSequenceIdOpt.isPresent()) {
      return postProcessDataFromDB(longToSequenceIdOpt.get());
    }

    Long nextGlobalSequenceId = sequenceIdService.getNextSequenceByKeyGenerator();
    UrlVO urlVO = convertSequenceIdToShortKey(nextGlobalSequenceId);

    redisService.setLongAndShort(longUrl, nextGlobalSequenceId.toString(), DEFAULT_CACHE_TTL);

    persistLongToShortAsync(
        longUrl, nextGlobalSequenceId.toString(), this::persistLongToSequenceId);

    return urlVO;
  }

  private UrlVO convertSequenceIdToShortKey(Long sequenceId) {
    String shortKey = tinyUrlGenerator.generate(sequenceId);
    UrlVO urlVo = createUrlVO(shortKey);
    return urlVo;
  }

  private String fetchNextAvailableShortUrl() {
    String shortUrl = null;

    while (true) {
      shortUrl = tinyUrlGenerator.generate();
      Optional<LongToShort> shortUrlOptional = longToShortRepository.findByShortUrl(shortUrl);
      if (shortUrlOptional.isPresent()) {
        continue;
      } else {
        break;
      }
    }

    return shortUrl;
  }

  private void persistLongToShortAsync(
      String longUrl, String shortUrl, BiFunction<String, String, Object> function) {
    executor.execute(
        () -> {
          function.apply(longUrl, shortUrl);
          System.out.println(Thread.currentThread().getName());
        });
  }

  @Transactional
  public Object persistLongToShort(String longUrl, String shortUrl) {
    LongToShort longToShort = new LongToShort();
    longToShort.setLongUrl(longUrl);
    longToShort.setShortUrl(shortUrl);

    longToShort = longToShortRepository.save(longToShort);
    return longToShort;
  }

  @Transactional
  public Object persistLongToSequenceId(String longUrl, String sequenceIdStr) {
    LongToSequenceId longToSequenceId = new LongToSequenceId();
    longToSequenceId.setLongUrl(longUrl);
    longToSequenceId.setSequenceId(NumberUtils.createLong(sequenceIdStr));

    longToSequenceId = longToSequenceIdRepository.save(longToSequenceId);
    return longToSequenceId;
  }

  private UrlVO postProcessDataFromDB(LongToShort longToShortData) {
    String longUrlMeta = longToShortData.getLongUrl();
    String shortUrlMeta = longToShortData.getShortUrl();

    redisService.setLongAndShort(longUrlMeta, shortUrlMeta, DEFAULT_CACHE_TTL);

    UrlVO urlVo = createUrlVO(shortUrlMeta);

    return urlVo;
  }

  private UrlVO createUrlVO(String shortUrlMeta) {
    UrlVO urlVo = new UrlVO();
    String fullUrl = constructTinyUrl(shortUrlMeta);
    urlVo.setUrl(fullUrl);
    return urlVo;
  }

  private UrlVO postProcessDataFromDB(LongToSequenceId longToSequenceId) {
    String longUrlMeta = longToSequenceId.getLongUrl();
    Long sequenceIdMeta = longToSequenceId.getSequenceId();

    redisService.setLongAndShort(longUrlMeta, sequenceIdMeta.toString(), DEFAULT_CACHE_TTL);

    UrlVO urlVo = convertSequenceIdToShortKey(sequenceIdMeta);

    return urlVo;
  }

  private UrlVO fetchTinyUrlFromCache(String url) {
    UrlVO urlVo = null;

    String shortExist = fetchValueByKey(url);

    if (!StringUtils.isEmpty(shortExist)) {
      urlVo = createUrlVO(shortExist);
    }

    return urlVo;
  }

  private String fetchValueByKey(String key) {
    String value = (String) redisService.get(key);
    redisService.expire(key, DEFAULT_CACHE_TTL);
    return value;
  }

  private String constructTinyUrl(String shortExist) {
    return shortUrlPrefix + shortExist;
  }

  /**
   * 短网址转长网址
   *
   * @param shortUrl
   * @return
   */
  @Override
  public String shortToLong(String shortUrl, HttpServletRequest request) {
    String longUrl = fetchLongUrl(shortUrl);
    return longUrl;
  }

  @Override
  public String shortToLong(String shortUrl) {
    String longUrl = null;
    Long sequenceId = tinyUrlGenerator.convertTinyUrlToId(shortUrl);
    longUrl = fetchLongUrl(sequenceId);

    return longUrl;
  }

  private String fetchLongUrl(String shortUrl) {
    String longUrl = (String) redisService.get(shortUrl);
    redisService.expire(shortUrl, 60);
    if (!StringUtils.isEmpty(longUrl)) {
      return longUrl;
    }

    Optional<LongToShort> longUrlOptional = longToShortRepository.findByShortUrl(shortUrl);

    if (longUrlOptional.isPresent()) {
      longUrl = longUrlOptional.get().getLongUrl();
      redisService.set(shortUrl, longUrl, 60);
    } else {
      longUrl = null;
    }

    return longUrl;
  }

  private String fetchLongUrl(Long sequenceId) {
    String longUrl = (String) redisService.get(sequenceId.toString());
    redisService.expire(sequenceId.toString(), 60);
    if (!StringUtils.isEmpty(longUrl)) {
      return longUrl;
    }

    Optional<LongToSequenceId> longToSequenceIdOpt =
        longToSequenceIdRepository.findBySequenceId(sequenceId);

    if (longToSequenceIdOpt.isPresent()) {
      longUrl = longToSequenceIdOpt.get().getLongUrl();
      redisService.set(sequenceId.toString(), longUrl, 60);
    } else {
      longUrl = null;
    }

    return longUrl;
  }
}
