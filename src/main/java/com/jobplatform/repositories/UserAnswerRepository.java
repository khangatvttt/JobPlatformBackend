package com.jobplatform.repositories;

import com.jobplatform.models.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    Optional<UserAnswer> findByQuizAttempt_IdAndQuestion_Id(Long quizAttemptId, Long questionId);

    List<UserAnswer> findByQuizAttemptId(Long quizAttemptId);
}
