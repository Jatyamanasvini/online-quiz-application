package com.example.quizplatform.dto;

/**
 * Public view of a course. Contains no answer keys.
 */
public record CourseSummary(
        Long id,
        String code,
        String title,
        String description,
        String icon,
        String accent,
        long topicCount,
        long questionCount) {
}
