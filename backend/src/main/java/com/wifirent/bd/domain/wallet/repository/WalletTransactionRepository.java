package com.wifirent.bd.domain.wallet.repository;

import com.wifirent.bd.domain.wallet.WalletTransaction;
import com.wifirent.bd.domain.wallet.WalletTransaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    Page<WalletTransaction> findByWalletId(Long walletId, Pageable pageable);

    List<WalletTransaction> findByWalletIdAndTransactionType(Long walletId, TransactionType type, Pageable pageable);

    Optional<WalletTransaction> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);
}
