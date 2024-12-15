package com.jobplatform.models.dto;

import java.time.LocalDateTime;

public record ReviewDto(
        Long id,
        Integer rating,
        String comment,
        Long jobId,
        Long userId,
        LocalDateTime createdAt) {
}
