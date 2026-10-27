package com.example.quizplatform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SubmitQuizRequest(
        @NotNull(message = "topicId is required") Long topicId,
        List<@Valid SubmittedAnswer> answers) {
}
