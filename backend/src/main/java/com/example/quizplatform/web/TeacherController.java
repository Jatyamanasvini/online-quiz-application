package com.example.quizplatform.web;

import com.example.quizplatform.dto.TeacherAttemptView;
import com.example.quizplatform.dto.TopicPerformanceView;
import com.example.quizplatform.service.AttemptService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final AttemptService attemptService;

    public TeacherController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @GetMapping("/attempts")
    public List<TeacherAttemptView> attempts() {
        return attemptService.allAttempts();
    }

    @GetMapping("/stats")
    public List<TopicPerformanceView> stats() {
        return attemptService.topicPerformance();
    }
}
