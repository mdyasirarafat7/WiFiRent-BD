package com.wifirent.bd.domain.session.repository;

import com.wifirent.bd.domain.session.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long> {
    List<UsageRecord> findBySessionId(String sessionId);
}
