package com.example.quizplatform.repository;

import com.example.quizplatform.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findAllByOrderByPositionAsc();

    Optional<Course> findByCode(String code);
}
