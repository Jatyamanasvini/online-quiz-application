package com.example.quizplatform.service;

import com.example.quizplatform.domain.AppUser;
import com.example.quizplatform.domain.AttemptAnswer;
import com.example.quizplatform.domain.Question;
import com.example.quizplatform.domain.QuizAttempt;
import com.example.quizplatform.domain.Topic;
import com.example.quizplatform.dto.AnswerReview;
import com.example.quizplatform.dto.AttemptSummary;
import com.example.quizplatform.dto.OptionView;
import com.example.quizplatform.dto.QuizResult;
import com.example.quizplatform.dto.TeacherAttemptView;
import com.example.quizplatform.dto.TopicPerformanceView;
import com.example.quizplatform.exception.NotFoundException;
import com.example.quizplatform.repository.QuizAttemptRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AttemptService {

    private final QuizAttemptRepository attemptRepository;

    public AttemptService(QuizAttemptRepository attemptRepository) {
        this.attemptRepository = attemptRepository;
    }

    @Transactional(readOnly = true)
    public List<AttemptSummary> myAttempts(AppUser user) {
        return attemptRepository.findAllByUserIdWithTopic(user.getId()).stream()
                .map(this::toSummary)
                .toList();
    }

    /**
     * Rebuilds the full review for one of the caller's own attempts. Teachers may view any
     * attempt; students may view only their own.
     */
    @Transactional(readOnly = true)
    public QuizResult attemptDetail(AppUser caller, Long attemptId) {
        QuizAttempt attempt = attemptRepository.findByIdWithTopic(attemptId)
                .orElseThrow(() -> new NotFoundException("Attempt " + attemptId + " was not found"));

        boolean isOwner = attempt.getUser().getId().equals(caller.getId());
        boolean isTeacher = caller.getRole() == com.example.quizplatform.domain.Role.TEACHER;
        if (!isOwner && !isTeacher) {
            throw new AccessDeniedException("You can only view your own attempts");
        }

        Topic topic = attempt.getTopic();
        List<AnswerReview> reviews = attempt.getAnswers().stream()
                .map(this::toReview)
                .toList();

        return new QuizResult(
                topic.getId(),
                topic.getTitle(),
                attempt.getTotal(),
                attempt.getCorrect(),
                attempt.getIncorrect(),
                attempt.getUnanswered(),
                attempt.getAccuracy(),
                reviews);
    }

    @Transactional(readOnly = true)
    public List<TeacherAttemptView> allAttempts() {
        return attemptRepository.findAllWithUserAndTopic().stream()
                .map(attempt -> new TeacherAttemptView(
                        attempt.getId(),
                        attempt.getUser().getName(),
                        attempt.getUser().getEmail(),
                        attempt.getTopic().getTitle(),
                        attempt.getTotal(),
                        attempt.getCorrect(),
                        attempt.getAccuracy(),
                        attempt.getSubmittedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TopicPerformanceView> topicPerformance() {
        return attemptRepository.findTopicPerformance().stream()
                .map(row -> new TopicPerformanceView(
                        row.getTopicId(),
                        row.getTopicTitle(),
                        row.getAttempts(),
                        Math.round(row.getAverageAccuracy() * 10.0) / 10.0,
                        row.getStudents()))
                .toList();
    }

    private AttemptSummary toSummary(QuizAttempt attempt) {
        Topic topic = attempt.getTopic();
        return new AttemptSummary(
                attempt.getId(),
                topic.getId(),
                topic.getTitle(),
                topic.getCourse().getTitle(),
                attempt.getTotal(),
                attempt.getCorrect(),
                attempt.getAccuracy(),
                attempt.getSubmittedAt());
    }

    private AnswerReview toReview(AttemptAnswer answer) {
        Question question = answer.getQuestion();
        return new AnswerReview(
                question.getId(),
                question.getText(),
                question.getOptions().stream()
                        .map(option -> new OptionView(option.getId(), option.getText()))
                        .toList(),
                answer.getSelectedOptionId(),
                answer.getCorrectOptionId(),
                answer.isAnsweredCorrectly(),
                question.getExplanation());
    }
}
