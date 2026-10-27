package com.example.quizplatform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "attempt_answers")
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "selected_option_id")
    private Long selectedOptionId;

    @Column(name = "correct_option_id", nullable = false)
    private Long correctOptionId;

    @Column(name = "answered_correctly", nullable = false)
    private boolean answeredCorrectly;

    protected AttemptAnswer() {
    }

    public AttemptAnswer(QuizAttempt attempt, Question question, Long selectedOptionId,
                         Long correctOptionId, boolean answeredCorrectly) {
        this.attempt = attempt;
        this.question = question;
        this.selectedOptionId = selectedOptionId;
        this.correctOptionId = correctOptionId;
        this.answeredCorrectly = answeredCorrectly;
    }

    public Long getId() {
        return id;
    }

    public QuizAttempt getAttempt() {
        return attempt;
    }

    public Question getQuestion() {
        return question;
    }

    public Long getSelectedOptionId() {
        return selectedOptionId;
    }

    public Long getCorrectOptionId() {
        return correctOptionId;
    }

    public boolean isAnsweredCorrectly() {
        return answeredCorrectly;
    }
}
