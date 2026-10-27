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

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_text", nullable = false, length = 500)
    private String text;

    @Column(nullable = false, length = 1000)
    private String explanation;

    @Column(name = "sort_order", nullable = false)
    private int position;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<AnswerOption> options = new ArrayList<>();

    protected Question() {
    }

    public Question(Topic topic, String text, String explanation, int position) {
        this.topic = topic;
        this.text = text;
        this.explanation = explanation;
        this.position = position;
    }

    public void addOption(AnswerOption option) {
        options.add(option);
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getExplanation() {
        return explanation;
    }

    public int getPosition() {
        return position;
    }

    public Topic getTopic() {
        return topic;
    }

    public List<AnswerOption> getOptions() {
        return options;
    }
}
