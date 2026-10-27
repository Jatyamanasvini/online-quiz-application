package com.example.quizplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

// Authentication is handled by JwtAuthFilter + AuthService with BCrypt, so the default
// in-memory user (and its generated password in the logs) is not needed.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class QuizPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuizPlatformApplication.class, args);
	}

}
