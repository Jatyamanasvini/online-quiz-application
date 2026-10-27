package com.example.quizplatform.repository;

import com.example.quizplatform.domain.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findByCourseIdOrderByPositionAsc(Long courseId);

    boolean existsByCourseId(Long courseId);

    Optional<Topic> findBySlug(String slug);

    @Query("select t.course.id as courseId, count(t) as total from Topic t group by t.course.id")
    List<CountByCourse> countTopicsByCourse();

    interface CountByCourse {
        Long getCourseId();

        Long getTotal();
    }
}
