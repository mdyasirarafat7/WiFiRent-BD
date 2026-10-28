package com.wifirent.bd.exception;

import java.time.Instant;
import java.util.Map;

public class ErrorResponse {

    private final int status;
    private final String code;
    private final String message;
    private final Instant timestamp;
    private final Map<String, String> fields;

    public ErrorResponse(int status, String code, String message, Instant timestamp,
                         Map<String, String> fields) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.fields = fields;
    }

    public int getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }
    public Map<String, String> getFields() { return fields; }
}
