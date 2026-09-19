package com.finassist.ai.dto;

import java.time.Instant;

public class ApiResponse<T> {

    private final String timestamp;
    private final T data;

    private ApiResponse(T data) {
        this.timestamp = Instant.now().toString();
        this.data = data;
    }

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data);
    }

    public String getTimestamp() {
        return timestamp;
    }

    public T getData() {
        return data;
    }
}