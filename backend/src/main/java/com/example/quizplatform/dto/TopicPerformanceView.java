package com.example.quizplatform.dto;

public record TopicPerformanceView(
        Long topicId,
        String topicTitle,
        long attempts,
        double averageAccuracy,
        long students) {
}
