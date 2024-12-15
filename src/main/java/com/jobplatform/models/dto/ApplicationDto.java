package com.jobplatform.models.dto;

import java.time.LocalDateTime;

public record ApplicationDto(
        Long id,
        Long jobId,
        Long userId,
        String status,
        Long cvId,
        String cvType,
        LocalDateTime appliedAt
) {
}
