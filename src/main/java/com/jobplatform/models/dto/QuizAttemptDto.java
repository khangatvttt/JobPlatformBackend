package com.jobplatform.models.dto;

import com.jobplatform.models.Question;

import java.time.LocalDateTime;
import java.util.List;

public record QuizAttemptDto(
        Long id,
        LocalDateTime startTime,
        List<Question> questions
) {
}
