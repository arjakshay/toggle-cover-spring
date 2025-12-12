package com.togglecover.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {
    private boolean success;
    private String message;
    private Object data;
    private String errorCode;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private String path;
    private Integer status;

    // Static factory methods for common responses
    public static ApiResponse success(String message) {
        return ApiResponse.builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ApiResponse success(String message, Object data) {
        return ApiResponse.builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ApiResponse error(String message) {
        return ApiResponse.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ApiResponse error(String message, String errorCode) {
        return ApiResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .build();
    }
}