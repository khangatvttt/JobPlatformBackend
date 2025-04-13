package com.jobplatform.models.dto;

import java.util.List;

public record QuestionDto(
        Long id,
        String content,
        String image,
        List<String> answers,
        String correctAnswer,
        String selectedAnswer,
        Boolean isCorrect
) {
}
