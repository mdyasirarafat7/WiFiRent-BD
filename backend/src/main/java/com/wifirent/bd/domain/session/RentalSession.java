package com.wifirent.bd.domain.session;

import com.wifirent.bd.domain.BaseEntity;
import com.wifirent.bd.domain.hotspot.Hotspot;
import com.wifirent.bd.domain.packageplan.PackagePlan;
import com.wifirent.bd.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "rental_sessions")
public class RentalSession extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotspot_id", nullable = false)
    private Hotspot hotspot;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private PackagePlan packagePlan;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rental_order_id", nullable = false)
    private com.wifirent.bd.domain.rental.RentalOrder rentalOrder;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "data_quota_bytes")
    private Long dataQuotaBytes;

    @Column(name = "consumed_bytes", nullable = false)
    private Long consumedBytes = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SessionStatus status = SessionStatus.PENDING;

    @Size(max = 255)
    @Column(name = "gateway_session_id", length = 255)
    private String gatewaySessionId;

    @Column(name = "gateway_device_id", length = 100)
    private String gatewayDeviceId;

    public RentalSession() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public Hotspot getHotspot() {
        return hotspot;
    }

    public void setHotspot(Hotspot hotspot) {
        this.hotspot = hotspot;
    }

    public PackagePlan getPackagePlan() {
        return packagePlan;
    }

    public void setPackagePlan(PackagePlan packagePlan) {
        this.packagePlan = packagePlan;
    }

    public com.wifirent.bd.domain.rental.RentalOrder getRentalOrder() {
        return rentalOrder;
    }

    public void setRentalOrder(com.wifirent.bd.domain.rental.RentalOrder rentalOrder) {
        this.rentalOrder = rentalOrder;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getDataQuotaBytes() {
        return dataQuotaBytes;
    }

    public void setDataQuotaBytes(Long dataQuotaBytes) {
        this.dataQuotaBytes = dataQuotaBytes;
    }

    public Long getConsumedBytes() {
        return consumedBytes;
    }

    public void setConsumedBytes(Long consumedBytes) {
        this.consumedBytes = consumedBytes;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public String getGatewaySessionId() {
        return gatewaySessionId;
    }

    public void setGatewaySessionId(String gatewaySessionId) {
        this.gatewaySessionId = gatewaySessionId;
    }

    public String getGatewayDeviceId() {
        return gatewayDeviceId;
    }

    public void setGatewayDeviceId(String gatewayDeviceId) {
        this.gatewayDeviceId = gatewayDeviceId;
    }
}

