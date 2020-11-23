package com.jiuzhang.url.db.repository;

import com.jiuzhang.url.db.entity.LongToSequenceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LongToSequenceIdRepository extends JpaRepository<LongToSequenceId, Long> {

    Optional<LongToSequenceId> findByLongUrl(String longUrl);

    Optional<LongToSequenceId> findBySequenceId(long sequenceId);
}
