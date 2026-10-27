package com.example.quizplatform.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(nullable = false)
    private int total;

    @Column(nullable = false)
    private int correct;

    @Column(nullable = false)
    private int incorrect;

    @Column(nullable = false)
    private int unanswered;

    @Column(nullable = false)
    private double accuracy;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<AttemptAnswer> answers = new ArrayList<>();

    protected QuizAttempt() {
    }

    public QuizAttempt(AppUser user, Topic topic, int total, int correct, int incorrect,
                       int unanswered, double accuracy) {
        this.user = user;
        this.topic = topic;
        this.total = total;
        this.correct = correct;
        this.incorrect = incorrect;
        this.unanswered = unanswered;
        this.accuracy = accuracy;
        this.submittedAt = Instant.now();
    }

    public void addAnswer(AttemptAnswer answer) {
        answers.add(answer);
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public Topic getTopic() {
        return topic;
    }

    public int getTotal() {
        return total;
    }

    public int getCorrect() {
        return correct;
    }

    public int getIncorrect() {
        return incorrect;
    }

    public int getUnanswered() {
        return unanswered;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public List<AttemptAnswer> getAnswers() {
        return answers;
    }
}
