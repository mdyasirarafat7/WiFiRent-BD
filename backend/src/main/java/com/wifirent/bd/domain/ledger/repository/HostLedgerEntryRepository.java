package com.wifirent.bd.domain.ledger.repository;

import com.wifirent.bd.domain.ledger.HostLedgerEntry;
import com.wifirent.bd.domain.user.User;
import com.wifirent.bd.domain.ledger.HostLedgerEntry.EntryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HostLedgerEntryRepository extends JpaRepository<HostLedgerEntry, Long> {
    Page<HostLedgerEntry> findByHostId(Long hostId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.amountMinorUnits), 0) FROM HostLedgerEntry e WHERE e.host.id = :hostId AND e.entryType = :type")
    Long sumByHostAndType(@Param("hostId") Long hostId, @Param("type") EntryType type);
}
