package com.example.quizplatform.web;

import com.example.quizplatform.dto.AttemptSummary;
import com.example.quizplatform.dto.QuizResult;
import com.example.quizplatform.service.AttemptService;
import com.example.quizplatform.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/attempts")
public class AttemptController {

    private final AttemptService attemptService;
    private final AuthService authService;

    public AttemptController(AttemptService attemptService, AuthService authService) {
        this.attemptService = attemptService;
        this.authService = authService;
    }

    @GetMapping("/my")
    public List<AttemptSummary> myAttempts(Authentication authentication) {
        return attemptService.myAttempts(authService.userByEmail(authentication.getName()));
    }

    @GetMapping("/{attemptId}")
    public QuizResult attemptDetail(Authentication authentication, @PathVariable Long attemptId) {
        return attemptService.attemptDetail(authService.userByEmail(authentication.getName()), attemptId);
    }
}
