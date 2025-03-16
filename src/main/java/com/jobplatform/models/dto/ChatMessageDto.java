package com.jobplatform.models.dto;

import java.time.LocalDateTime;

public record ChatMessageDto(
        Long id,
        String sender,
        String receiver,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt,
        boolean isDeleted
) {
}
