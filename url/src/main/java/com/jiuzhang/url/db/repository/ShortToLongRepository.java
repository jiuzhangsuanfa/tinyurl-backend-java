package com.jiuzhang.url.db.repository;

import com.jiuzhang.url.db.entity.LongToShort;
import com.jiuzhang.url.db.entity.ShortToLong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortToLongRepository extends JpaRepository<ShortToLong, Long> {
    
    Optional<ShortToLong> findByShortUrl(String shortUrl);
}
