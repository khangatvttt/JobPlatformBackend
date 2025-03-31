package com.jobplatform.services;

import com.jobplatform.models.Question;
import com.jobplatform.models.QuizAttempt;
import com.jobplatform.models.UserAccount;
import com.jobplatform.models.UserAnswer;
import com.jobplatform.models.dto.QuizAttemptAnswerDto;
import com.jobplatform.models.dto.QuizAttemptDto;
import com.jobplatform.models.dto.QuizAttemptMapper;
import com.jobplatform.models.dto.UserAnswerDto;
import com.jobplatform.repositories.QuestionRepository;
import com.jobplatform.repositories.QuizAttemptRepository;
import com.jobplatform.repositories.UserAnswerRepository;
import com.jobplatform.repositories.UserRepository;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.naming.NoPermissionException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuestionRepository questionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final QuizAttemptMapper quizAttemptMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ThreadPoolTaskScheduler scheduler;
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> activeTimers = new ConcurrentHashMap<>();

    public QuizService(QuizAttemptRepository quizAttemptRepository, UserRepository userRepository, QuestionRepository questionRepository, UserAnswerRepository userAnswerRepository, QuizAttemptMapper quizAttemptMapper, SimpMessagingTemplate messagingTemplate) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.questionRepository = questionRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.quizAttemptMapper = quizAttemptMapper;
        this.messagingTemplate = messagingTemplate;
        this.scheduler = new ThreadPoolTaskScheduler();
        this.scheduler.initialize();
    }

    public void startTimer(Long quizAttemptId, int durationSeconds) {
        AtomicInteger remainingTime = new AtomicInteger(durationSeconds);

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            int timeLeft = remainingTime.decrementAndGet();

            if (timeLeft <= 0) {
                autoSubmit(quizAttemptId);
            } else {
                messagingTemplate.convertAndSend("/quiz/" + quizAttemptId + "/time", "{\"timeLeft\": " + timeLeft + "}");
            }
        }, new PeriodicTrigger(1, TimeUnit.SECONDS));

        activeTimers.put(quizAttemptId, future);
    }

    @SneakyThrows
    public void autoSubmit(Long quizAttemptId) {
        QuizAttempt quizAttempt = quizAttemptRepository.findById(quizAttemptId).orElse(null);
        if (quizAttempt == null || quizAttempt.getSubmittedAt() != null) {
            throw new BadRequestException("Not valid submit request");
        }

        ScheduledFuture<?> future = activeTimers.remove(quizAttemptId);
        if (future != null) {
            future.cancel(false);
        }

        quizAttempt.setSubmittedAt(LocalDateTime.now());
        List<UserAnswer> answers = userAnswerRepository.findByQuizAttemptId(quizAttemptId);
        List<UserAnswer> validatedAnswer = answers.stream().map(this::validationAnswer).toList();
        int score = (int) validatedAnswer.stream().filter(ans -> ans.getCorrect() != null && ans.getCorrect()).count();

        quizAttempt.getUserAnswers().clear();
        quizAttempt.getUserAnswers().addAll(validatedAnswer);
        quizAttemptRepository.save(quizAttempt);
        userAnswerRepository.saveAll(validatedAnswer);

        messagingTemplate.convertAndSend("/quiz/result/" + quizAttemptId, "Quiz submitted. Score: " + score);
        activeTimers.remove(quizAttemptId);
    }

    private UserAnswer validationAnswer(UserAnswer userAnswer) {
        userAnswer.setCorrect(userAnswer.getQuestion().getCorrectAnswer().equals(userAnswer.getSelectedAnswer()));
        return userAnswer;
    }

    public QuizAttemptDto startQuiz(Integer timeLimit, Integer numberOfQuestion) {
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        QuizAttempt quizAttempt = new QuizAttempt();
        quizAttempt.setUser(userAccount);
        quizAttempt.setStartTime(LocalDateTime.now());
        quizAttempt.setTimeLimit(timeLimit);
        QuizAttempt savedquizAttempt = quizAttemptRepository.save(quizAttempt);

        List<Question> questionList = questionRepository.findRandomQuestions(numberOfQuestion);
        List<UserAnswer> userAnswers = new ArrayList<>(questionList.stream()
                .map(question -> {
                    UserAnswer userAnswer = new UserAnswer();
                    userAnswer.setQuizAttempt(savedquizAttempt);
                    userAnswer.setQuestion(question);
                    userAnswer.setSelectedAnswer(null);
                    userAnswer.setCorrect(null);
                    return userAnswer;
                })
                .toList());
        userAnswerRepository.saveAll(userAnswers);
        savedquizAttempt.getUserAnswers().clear();
        savedquizAttempt.getUserAnswers().addAll(userAnswers);

        QuizAttempt result = quizAttemptRepository.save(savedquizAttempt);
        return quizAttemptMapper.toDto(result);
    }

    public List<QuizAttemptAnswerDto> getAllQuizAttempt() {
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<QuizAttempt> quizAttemptList = quizAttemptRepository.findByUser_Id(userAccount.getId());
        return quizAttemptList.stream().map(quizAttemptMapper::toAnswerDto).toList();
    }

    @SneakyThrows
    public QuizAttemptAnswerDto getQuizAttempt(Long id) {
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        QuizAttempt quiz = quizAttemptRepository.findById(id).orElseThrow(()-> new NoSuchElementException("Quiz not found"));
        if (quiz.getUser().getId().equals(userAccount.getId())) {
            throw new NoPermissionException("No permission");
        }
        return quizAttemptMapper.toAnswerDto(quiz);
    }

    public void saveAnswer(UserAnswerDto userAnswerDto) {
        UserAnswer userAnswer = userAnswerRepository.findByQuizAttempt_IdAndQuestion_Id(userAnswerDto.quizAttemptId(), userAnswerDto.questionId())
                .orElseThrow(() -> new NoSuchElementException("Wrong quiz attempt Id or question Id"));
        userAnswer.setSelectedAnswer(userAnswerDto.answer());
        userAnswerRepository.save(userAnswer);
    }

    @SneakyThrows
    public void checkPermission(Long quizId) {
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        QuizAttempt quizAttempt = quizAttemptRepository.findById(quizId).orElseThrow(() -> new NoSuchElementException("Quiz attempt Id not found"));
        if (!quizAttempt.getUser().getId().equals(userAccount.getId())) {
            throw new NoPermissionException("Can access other quiz");
        }
    }
}

