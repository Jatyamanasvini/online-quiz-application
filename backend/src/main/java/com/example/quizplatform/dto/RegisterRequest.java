package com.example.quizplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "name is required") @Size(max = 120) String name,
        @NotBlank(message = "email is required") @Email(message = "email is not valid") String email,
        @NotBlank(message = "password is required") @Size(min = 6, max = 72,
                message = "password must be at least 6 characters") String password) {
}
