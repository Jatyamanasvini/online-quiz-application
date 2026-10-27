package com.example.quizplatform.dto;

public record TopicSummary(
        Long id,
        String slug,
        String title,
        String description,
        long questionCount) {
}
