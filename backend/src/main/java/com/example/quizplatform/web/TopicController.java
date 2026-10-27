package com.example.quizplatform.web;

import com.example.quizplatform.dto.QuizView;
import com.example.quizplatform.service.QuizService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final QuizService quizService;

    public TopicController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/{topicId}/quiz")
    public QuizView getQuiz(@PathVariable Long topicId) {
        return quizService.getQuiz(topicId);
    }
}
