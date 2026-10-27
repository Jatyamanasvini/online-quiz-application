package com.example.quizplatform.dto;

import jakarta.validation.constraints.NotNull;

/**
 * A single selected option. {@code optionId} may be null for an unanswered question.
 */
public record SubmittedAnswer(
        @NotNull(message = "questionId is required") Long questionId,
        Long optionId) {
}
