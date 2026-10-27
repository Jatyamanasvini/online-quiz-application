package com.example.quizplatform.dto;

public record UserView(
        Long id,
        String name,
        String email,
        String role) {
}
