package com.example.quizplatform.dto;

import java.time.Instant;

public record TeacherAttemptView(
        Long id,
        String studentName,
        String studentEmail,
        String topicTitle,
        int total,
        int correct,
        double accuracy,
        Instant submittedAt) {
}
