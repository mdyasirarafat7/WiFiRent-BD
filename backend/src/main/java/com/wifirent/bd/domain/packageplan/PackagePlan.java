package com.wifirent.bd.domain.packageplan;

import com.wifirent.bd.domain.BaseEntity;
import com.wifirent.bd.domain.hotspot.Hotspot;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "package_plans")
public class PackagePlan extends BaseEntity {

    @NotNull
    @DecimalMin("0")
    @Column(name = "price_minor_units", nullable = false)
    private Long priceMinorUnits;

    @Enumerated(EnumType.STRING)
    @Column(name = "package_type", nullable = false, length = 20)
    private PackageType packageType;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "data_quota_bytes")
    private Long dataQuotaBytes;

    @Column(name = "validity_minutes")
    private Integer validityMinutes;

    @Column(name = "speed_limit_kbps")
    private Integer speedLimitKbps;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotspot_id", nullable = false)
    @JsonIgnore
    private Hotspot hotspot;

    public PackagePlan() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPriceMinorUnits() {
        return priceMinorUnits;
    }

    public void setPriceMinorUnits(Long priceMinorUnits) {
        this.priceMinorUnits = priceMinorUnits;
    }

    public PackageType getPackageType() {
        return packageType;
    }

    public void setPackageType(PackageType packageType) {
        this.packageType = packageType;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Long getDataQuotaBytes() {
        return dataQuotaBytes;
    }

    public void setDataQuotaBytes(Long dataQuotaBytes) {
        this.dataQuotaBytes = dataQuotaBytes;
    }

    public Integer getValidityMinutes() {
        return validityMinutes;
    }

    public void setValidityMinutes(Integer validityMinutes) {
        this.validityMinutes = validityMinutes;
    }

    public Integer getSpeedLimitKbps() {
        return speedLimitKbps;
    }

    public void setSpeedLimitKbps(Integer speedLimitKbps) {
        this.speedLimitKbps = speedLimitKbps;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Hotspot getHotspot() {
        return hotspot;
    }

    public void setHotspot(Hotspot hotspot) {
        this.hotspot = hotspot;
    }
}



