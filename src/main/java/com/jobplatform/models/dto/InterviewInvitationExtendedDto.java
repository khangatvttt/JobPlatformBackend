package com.jobplatform.models.dto;

import java.time.LocalDateTime;

public record InterviewInvitationExtendedDto(
        Long id,
        String content,
        LocalDateTime createAt,
        Long userId,
        Long jobId
) {
}
