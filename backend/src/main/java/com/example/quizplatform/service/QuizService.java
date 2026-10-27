package com.example.quizplatform.service;

import com.example.quizplatform.domain.AnswerOption;
import com.example.quizplatform.domain.AppUser;
import com.example.quizplatform.domain.AttemptAnswer;
import com.example.quizplatform.domain.Course;
import com.example.quizplatform.domain.Question;
import com.example.quizplatform.domain.QuizAttempt;
import com.example.quizplatform.domain.Topic;
import com.example.quizplatform.dto.AnswerReview;
import com.example.quizplatform.dto.CourseSummary;
import com.example.quizplatform.dto.OptionView;
import com.example.quizplatform.dto.QuestionView;
import com.example.quizplatform.dto.QuizResult;
import com.example.quizplatform.dto.QuizView;
import com.example.quizplatform.dto.SubmitQuizRequest;
import com.example.quizplatform.dto.SubmittedAnswer;
import com.example.quizplatform.dto.TopicSummary;
import com.example.quizplatform.exception.BadRequestException;
import com.example.quizplatform.exception.NotFoundException;
import com.example.quizplatform.exception.QuizUnavailableException;
import com.example.quizplatform.repository.CourseRepository;
import com.example.quizplatform.repository.QuestionRepository;
import com.example.quizplatform.repository.QuizAttemptRepository;
import com.example.quizplatform.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class QuizService {

    private final CourseRepository courseRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;

    public QuizService(CourseRepository courseRepository,
                       TopicRepository topicRepository,
                       QuestionRepository questionRepository,
                       QuizAttemptRepository attemptRepository) {
        this.courseRepository = courseRepository;
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseSummary> listCourses() {
        Map<Long, Long> topicCounts = new HashMap<>();
        topicRepository.countTopicsByCourse()
                .forEach(row -> topicCounts.put(row.getCourseId(), row.getTotal()));

        Map<Long, Long> questionCounts = new HashMap<>();
        questionRepository.countQuestionsByCourse()
                .forEach(row -> questionCounts.put(row.getCourseId(), row.getTotal()));

        return courseRepository.findAllByOrderByPositionAsc().stream()
                .map(course -> new CourseSummary(
                        course.getId(),
                        course.getCode(),
                        course.getTitle(),
                        course.getDescription(),
                        course.getIcon(),
                        course.getAccent(),
                        topicCounts.getOrDefault(course.getId(), 0L),
                        questionCounts.getOrDefault(course.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TopicSummary> listTopics(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course " + courseId + " was not found"));

        Map<Long, Long> questionCounts = new HashMap<>();
        questionRepository.countQuestionsByTopicInCourse(course.getId())
                .forEach(row -> questionCounts.put(row.getTopicId(), row.getTotal()));

        return topicRepository.findByCourseIdOrderByPositionAsc(course.getId()).stream()
                .map(topic -> new TopicSummary(
                        topic.getId(),
                        topic.getSlug(),
                        topic.getTitle(),
                        topic.getDescription(),
                        questionCounts.getOrDefault(topic.getId(), 0L)))
                .toList();
    }

    /**
     * Returns the quiz payload for a topic. Correctness flags and explanations are never
     * included; they only appear in the submission response.
     */
    @Transactional(readOnly = true)
    public QuizView getQuiz(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NotFoundException("Topic " + topicId + " was not found"));

        List<Question> questions = questionRepository.findByTopicIdOrderByPositionAsc(topicId);
        if (questions.isEmpty()) {
            throw new QuizUnavailableException("No questions are available for this topic yet");
        }

        Course course = topic.getCourse();
        return new QuizView(
                topic.getId(),
                topic.getTitle(),
                course.getId(),
                course.getTitle(),
                questions.size(),
                questions.stream().map(this::toQuestionView).toList());
    }

    /**
     * Scores a submission and persists it as a {@link QuizAttempt} for the given user.
     */
    @Transactional
    public QuizResult submit(SubmitQuizRequest request, AppUser user) {
        if (request == null || request.topicId() == null) {
            throw new BadRequestException("topicId is required");
        }

        Topic topic = topicRepository.findById(request.topicId())
                .orElseThrow(() -> new NotFoundException("Topic " + request.topicId() + " was not found"));

        List<Question> questions = questionRepository.findByTopicIdOrderByPositionAsc(topic.getId());
        if (questions.isEmpty()) {
            throw new QuizUnavailableException("No questions are available for this topic yet");
        }

        Map<Long, Long> selections = collectSelections(request.answers());

        Set<Long> questionIds = new HashSet<>();
        questions.forEach(question -> questionIds.add(question.getId()));
        for (Long questionId : selections.keySet()) {
            if (!questionIds.contains(questionId)) {
                throw new BadRequestException(
                        "Question " + questionId + " does not belong to topic " + topic.getId());
            }
        }

        Map<Long, AnswerOption> optionsById = new HashMap<>();
        questions.forEach(question -> question.getOptions().forEach(option -> optionsById.put(option.getId(), option)));

        int correct = 0;
        int unanswered = 0;
        List<AnswerReview> reviews = new ArrayList<>();
        List<AttemptAnswer> gradedAnswers = new ArrayList<>();

        for (Question question : questions) {
            Long selectedOptionId = selections.get(question.getId());
            if (selectedOptionId != null) {
                AnswerOption selected = optionsById.get(selectedOptionId);
                if (selected == null || !selected.getQuestion().getId().equals(question.getId())) {
                    throw new BadRequestException(
                            "Option " + selectedOptionId + " does not belong to question " + question.getId());
                }
            } else {
                unanswered++;
            }

            AnswerOption correctOption = question.getOptions().stream()
                    .filter(AnswerOption::isCorrect)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Question " + question.getId() + " has no correct option"));

            boolean isCorrect = selectedOptionId != null && selectedOptionId.equals(correctOption.getId());
            if (isCorrect) {
                correct++;
            }

            reviews.add(new AnswerReview(
                    question.getId(),
                    question.getText(),
                    question.getOptions().stream()
                            .map(option -> new OptionView(option.getId(), option.getText()))
                            .toList(),
                    selectedOptionId,
                    correctOption.getId(),
                    isCorrect,
                    question.getExplanation()));

            gradedAnswers.add(new AttemptAnswer(null, question, selectedOptionId,
                    correctOption.getId(), isCorrect));
        }

        int total = questions.size();
        int incorrect = total - correct;
        double accuracy = Math.round((correct * 1000.0) / total) / 10.0;

        QuizAttempt attempt = new QuizAttempt(user, topic, total, correct, incorrect, unanswered, accuracy);
        gradedAnswers.forEach(answer -> attempt.addAnswer(new AttemptAnswer(
                attempt, answer.getQuestion(), answer.getSelectedOptionId(),
                answer.getCorrectOptionId(), answer.isAnsweredCorrectly())));
        attemptRepository.save(attempt);

        return new QuizResult(
                topic.getId(),
                topic.getTitle(),
                total,
                correct,
                incorrect,
                unanswered,
                accuracy,
                reviews);
    }

    private Map<Long, Long> collectSelections(List<SubmittedAnswer> answers) {
        Map<Long, Long> selections = new LinkedHashMap<>();
        if (answers == null) {
            return selections;
        }
        for (SubmittedAnswer answer : answers) {
            if (answer == null || answer.questionId() == null) {
                throw new BadRequestException("Every answer needs a questionId");
            }
            if (selections.containsKey(answer.questionId())) {
                throw new BadRequestException("Duplicate answer for question " + answer.questionId());
            }
            selections.put(answer.questionId(), answer.optionId());
        }
        return selections;
    }

    private QuestionView toQuestionView(Question question) {
        return new QuestionView(
                question.getId(),
                question.getText(),
                question.getOptions().stream()
                        .map(option -> new OptionView(option.getId(), option.getText()))
                        .toList());
    }
}
