package com.example.quizplatform.dto;

import java.util.List;

/**
 * Quiz payload sent before submission. Deliberately excludes correctness flags
 * and explanations so the client cannot reveal answers early.
 */
public record QuizView(
        Long topicId,
        String topicTitle,
        Long courseId,
        String courseTitle,
        int questionCount,
        List<QuestionView> questions) {
}
