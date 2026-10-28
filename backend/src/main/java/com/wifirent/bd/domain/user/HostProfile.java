package com.wifirent.bd.domain.user;

import com.wifirent.bd.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "host_profiles")
public class HostProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "business_name", length = 200)
    private String businessName;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "is_approved", nullable = false)
    private Boolean approved = false;

    @Column(name = "commission_rate_bps", nullable = false)
    private Integer commissionRateBps = 100; // 1.00% default in basis points

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private HostStatus status = HostStatus.PENDING;

    public HostProfile() {
    }

    public enum HostStatus {
        PENDING, APPROVED, SUSPENDED, REJECTED
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

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public Integer getCommissionRateBps() {
        return commissionRateBps;
    }

    public void setCommissionRateBps(Integer commissionRateBps) {
        this.commissionRateBps = commissionRateBps;
    }

    public HostStatus getStatus() {
        return status;
    }

    public void setStatus(HostStatus status) {
        this.status = status;
    }
}
