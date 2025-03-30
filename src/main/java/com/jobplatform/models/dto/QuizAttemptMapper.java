package com.jobplatform.models.dto;


import com.jobplatform.models.Question;
import com.jobplatform.models.QuizAttempt;
import com.jobplatform.models.UserAnswer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface QuizAttemptMapper {

    QuizAttemptMapper INSTANCE = Mappers.getMapper(QuizAttemptMapper.class);

    @Mapping(target = "questions", expression = "java(mapQuestions(quizAttempt.getUserAnswers()))")
    QuizAttemptDto toDto(QuizAttempt quizAttempt);

    default List<Question> mapQuestions(List<UserAnswer> userAnswers) {
        List<UserAnswer> mutableList = new ArrayList<>(userAnswers);
        return mutableList.stream()
                .map(userAnswer -> removeCorrectAnswer(userAnswer.getQuestion()))
                .collect(Collectors.toList());
    }

    default Question removeCorrectAnswer(Question question) {
        question.setCorrectAnswer(null);
        return question;
    }

    @Mapping(source = "id", target = "id")
    @Mapping(source = "startTime", target = "startTime")
    @Mapping(source = "timeLimit", target = "timeLimit")
    @Mapping(source = "submittedAt", target = "submittedAt")
    @Mapping(target = "questions", expression = "java(mapAnswerQuestions(quizAttempt.getUserAnswers()))")
    QuizAttemptAnswerDto toAnswerDto(QuizAttempt quizAttempt);

    default List<QuestionDto> mapAnswerQuestions(List<UserAnswer> userAnswers) {
        return userAnswers.stream()
                .map(userAnswer -> new QuestionDto(
                        userAnswer.getQuestion().getId(),
                        userAnswer.getQuestion().getContent(),
                        userAnswer.getQuestion().getAnswers(),
                        userAnswer.getQuestion().getCorrectAnswer(),
                        userAnswer.getSelectedAnswer(),
                        userAnswer.getCorrect()
                ))
                .collect(Collectors.toList());
    }
}


