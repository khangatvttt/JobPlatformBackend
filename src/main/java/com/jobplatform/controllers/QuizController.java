package com.jobplatform.controllers;

import com.jobplatform.models.QuizAttempt;
import com.jobplatform.models.dto.QuizAttemptAnswerDto;
import com.jobplatform.models.dto.QuizAttemptDto;
import com.jobplatform.services.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/quiz")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    // Create a new quiz attempt
    @PostMapping("/start")
    public ResponseEntity<QuizAttemptDto> startQuiz(@RequestBody Map<String, Integer> payload) {
        Integer timeLimit = payload.get("timeLimit");
        Integer numberOfQuestion = payload.get("numberOfQuestion");
        QuizAttemptDto quizDto = quizService.startQuiz(timeLimit, numberOfQuestion);
        quizService.startTimer(quizDto.id(), timeLimit);
        return ResponseEntity.ok(quizDto);
    }

    // Get active quiz attempts
    @GetMapping("")
    public ResponseEntity<List<QuizAttemptAnswerDto>> getUserQuizAttempts() {
        return ResponseEntity.ok(quizService.getAllQuizAttempt());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizAttemptAnswerDto> getQuizAttempts(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuizAttempt(id));
    }

    // Submit quiz
    @PostMapping("/{quizAttemptId}/submit")
    public ResponseEntity<String> submitQuiz(@PathVariable Long quizAttemptId) {
        quizService.checkPermission(quizAttemptId);
        quizService.autoSubmit(quizAttemptId);
        return ResponseEntity.ok("Quiz submitted successfully");
    }
}

