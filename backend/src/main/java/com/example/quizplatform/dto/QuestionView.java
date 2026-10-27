package com.example.quizplatform.dto;

import java.util.List;

public record QuestionView(
        Long id,
        String text,
        List<OptionView> options) {
}
