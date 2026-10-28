package com.wifirent.bd.domain.rental;

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
import java.time.Instant;

@Entity
@Table(name = "rental_orders")
public class RentalOrder extends BaseEntity {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RentalOrderStatus status = RentalOrderStatus.PENDING;

    @NotNull
    @Column(name = "total_price_minor_units", nullable = false)
    private Long totalPriceMinorUnits;

    @Column(name = "idempotency_key", length = 255, unique = true)
    private String idempotencyKey;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    public RentalOrder() {
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

    public RentalOrderStatus getStatus() {
        return status;
    }

    public void setStatus(RentalOrderStatus status) {
        this.status = status;
    }

    public Long getTotalPriceMinorUnits() {
        return totalPriceMinorUnits;
    }

    public void setTotalPriceMinorUnits(Long totalPriceMinorUnits) {
        this.totalPriceMinorUnits = totalPriceMinorUnits;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}

