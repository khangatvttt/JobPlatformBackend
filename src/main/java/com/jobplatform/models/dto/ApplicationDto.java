package com.jobplatform.models.dto;

import java.time.LocalDateTime;

public record ApplicationDto(
        Long id,
        Long jobId,
        Long userId,
        String status,
        String companyImage,
        String companyName,
        String recruiterName,
        String recruiterEmail,
        Long cvId,
        String cvType,
        LocalDateTime appliedAt
) {
}
