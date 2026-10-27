package com.example.quizplatform.web;

import com.example.quizplatform.dto.CourseSummary;
import com.example.quizplatform.dto.TopicSummary;
import com.example.quizplatform.service.QuizService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final QuizService quizService;

    public CourseController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public List<CourseSummary> listCourses() {
        return quizService.listCourses();
    }

    @GetMapping("/{courseId}/topics")
    public List<TopicSummary> listTopics(@PathVariable Long courseId) {
        return quizService.listTopics(courseId);
    }
}
