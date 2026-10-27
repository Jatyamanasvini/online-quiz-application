package com.example.quizplatform.dto;

import java.time.Instant;

public record AttemptSummary(
        Long id,
        Long topicId,
        String topicTitle,
        String courseTitle,
        int total,
        int correct,
        double accuracy,
        Instant submittedAt) {
}
