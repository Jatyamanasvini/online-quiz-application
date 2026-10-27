package com.example.quizplatform.dto;

import java.util.List;

public record QuizResult(
        Long topicId,
        String topicTitle,
        int total,
        int correct,
        int incorrect,
        int unanswered,
        double accuracy,
        List<AnswerReview> results) {
}
