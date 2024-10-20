package com.jobplatform.models.dto;

public record LoginTokenDto(
        String accessToken,
        String refreshToken
) {
}
