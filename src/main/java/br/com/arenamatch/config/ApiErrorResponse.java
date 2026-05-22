package br.com.arenamatch.config;

import java.time.LocalDateTime;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String code,
        String message) {
}
