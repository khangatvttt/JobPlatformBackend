package com.jobplatform.models.dto;

import java.time.LocalDateTime;

public record JobSaveDto(
        Long jobSaveId,
        LocalDateTime savedAt,
        Long userId,
        Long jobId
) {
}
