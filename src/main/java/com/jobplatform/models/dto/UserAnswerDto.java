package com.jobplatform.models.dto;

public record UserAnswerDto(
        Long quizAttemptId,
        Long questionId,
        String answer
) {
}
