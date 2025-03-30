package com.jobplatform.controllers;

import com.jobplatform.models.UserAnswer;
import com.jobplatform.models.dto.UserAnswerDto;
import com.jobplatform.repositories.UserAnswerRepository;
import com.jobplatform.services.QuizService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Controller
public class QuizWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final QuizService quizService;

    public QuizWebSocketController(SimpMessagingTemplate messagingTemplate, QuizService quizService) {
        this.messagingTemplate = messagingTemplate;
        this.quizService = quizService;
    }

    @MessageMapping("/quiz/answer")
    @Transactional
    public void saveAnswer(@Payload UserAnswerDto userAnswerDto) {
        quizService.saveAnswer(userAnswerDto);
        messagingTemplate.convertAndSend("/quiz/progress/" + userAnswerDto.quizAttemptId(), "Answer saved");
    }
}
