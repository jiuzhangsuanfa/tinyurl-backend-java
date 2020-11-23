package com.jiuzhang.url.db.repository;

import com.jiuzhang.url.db.entity.LongToShort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LongToShortRepository extends JpaRepository<LongToShort, Long> {

    Optional<LongToShort> findByLongUrl(String longUrl);

    Optional<LongToShort> findByShortUrl(String shortUrl);
}
