package com.wifirent.bd.domain.wallet;

import com.wifirent.bd.domain.BaseEntity;
import com.wifirent.bd.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "wallets")
public class Wallet extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotNull
    @Column(name = "balance_minor_units", nullable = false)
    private Long balanceMinorUnits = 0L;

    public Wallet() {
    }

    public Wallet(User user, Long initialBalance) {
        this.user = user;
        this.balanceMinorUnits = initialBalance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getBalanceMinorUnits() {
        return balanceMinorUnits;
    }

    public void setBalanceMinorUnits(Long balanceMinorUnits) {
        this.balanceMinorUnits = balanceMinorUnits;
    }

    public void credit(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        this.balanceMinorUnits += amount;
    }

    public void debit(Long amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        if (this.balanceMinorUnits < amount) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.balanceMinorUnits -= amount;
    }
}
