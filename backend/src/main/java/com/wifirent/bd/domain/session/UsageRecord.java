package com.wifirent.bd.domain.session;

import com.wifirent.bd.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "usage_records")
public class UsageRecord extends BaseEntity {

    @Column(name = "session_id", nullable = false, length = 255)
    private String sessionId;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "consumed_bytes", nullable = false)
    private Long consumedBytes;

    @Column(name = "duration_seconds", nullable = false)
    private Long durationSeconds;

    @Column(name = "source", length = 50)
    private String source = "gateway";

    public UsageRecord() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    public Long getConsumedBytes() {
        return consumedBytes;
    }

    public void setConsumedBytes(Long consumedBytes) {
        this.consumedBytes = consumedBytes;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
