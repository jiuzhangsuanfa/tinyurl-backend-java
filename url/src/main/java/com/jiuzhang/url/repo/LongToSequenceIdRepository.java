package com.jiuzhang.url.repo;

import com.jiuzhang.url.domain.LongToSequenceId;
import com.jiuzhang.url.domain.LongToShort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LongToSequenceIdRepository extends JpaRepository<LongToSequenceId, Long> {

    Optional<LongToSequenceId> findByLongUrl(String longUrl);

    Optional<LongToSequenceId> findBySequenceId(long sequenceId);
}
