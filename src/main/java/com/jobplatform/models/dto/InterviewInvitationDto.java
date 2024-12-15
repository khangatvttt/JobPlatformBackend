package com.jobplatform.models.dto;

import java.time.LocalDateTime;

public record InterviewInvitationDto(
        Long id,
        String content,
        Long applicationId,
        LocalDateTime createAt) {
}
