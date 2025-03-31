package com.jobplatform.models.dto;

import com.jobplatform.models.Question;

import java.time.LocalDateTime;
import java.util.List;

public record QuizAttemptAnswerDto(
        Long id,
        LocalDateTime startTime,
        Long timeLimit,
        LocalDateTime submittedAt,
        List<QuestionDto> questions

) {
}
