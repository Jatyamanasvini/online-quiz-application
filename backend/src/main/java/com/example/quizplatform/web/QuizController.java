package com.example.quizplatform.web;

import com.example.quizplatform.dto.QuizResult;
import com.example.quizplatform.dto.SubmitQuizRequest;
import com.example.quizplatform.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/submit")
    @ResponseStatus(HttpStatus.OK)
    public QuizResult submit(@Valid @RequestBody SubmitQuizRequest request) {
        return quizService.submit(request);
    }
}
