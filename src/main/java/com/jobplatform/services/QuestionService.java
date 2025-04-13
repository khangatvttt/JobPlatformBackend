package com.jobplatform.services;

import com.jobplatform.models.Question;
import com.jobplatform.models.dto.QuestionMapper;
import com.jobplatform.repositories.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Question not found with id: " + id));
    }

    @SneakyThrows
    public Question createQuestion(Question question) {
        if (!question.getAnswers().contains(question.getCorrectAnswer())) {
            throw new BadRequestException("Correct answer must be in answer options");
        }
        return questionRepository.save(question);
    }

    @SneakyThrows
    public Question updateQuestion(Long id, Question partialUpdate) {
        Question existingQuestion = getQuestionById(id);
        questionMapper.updateQuestion(partialUpdate, existingQuestion);
        if (!existingQuestion.getAnswers().contains(existingQuestion.getCorrectAnswer())) {
            throw new BadRequestException("Correct answer must be in answer options");
        }
        return questionRepository.save(existingQuestion);
    }
    public void deleteQuestion(Long id) {
        Question existingQuestion = getQuestionById(id);
        existingQuestion.setStatus(false);
        questionRepository.save(existingQuestion);
    }
}

