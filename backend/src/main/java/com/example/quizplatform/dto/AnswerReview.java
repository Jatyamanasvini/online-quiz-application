package com.example.quizplatform.dto;

import java.util.List;

/**
 * Review data for one question. Only produced by the submit endpoint.
 */
public record AnswerReview(
        Long questionId,
        String questionText,
        List<OptionView> options,
        Long selectedOptionId,
        Long correctOptionId,
        boolean correct,
        String explanation) {
}
