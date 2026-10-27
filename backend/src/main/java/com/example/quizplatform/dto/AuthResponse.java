package com.example.quizplatform.dto;

public record AuthResponse(
        String token,
        UserView user) {
}
