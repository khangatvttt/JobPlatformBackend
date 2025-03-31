package com.jobplatform.models.dto;

import java.util.List;

public record QuestionDto(
        Long id,
        String content,
        List<String> answers,
        String correctAnswer,
        String selectedAnswer,
        Boolean isCorrect
) {
}
