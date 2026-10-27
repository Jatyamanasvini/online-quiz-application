package com.example.quizplatform.repository;

import com.example.quizplatform.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByTopicIdOrderByPositionAsc(Long topicId);

    long countByTopicId(Long topicId);

    @Query("select q.topic.id as topicId, count(q) as total from Question q "
            + "where q.topic.course.id = :courseId group by q.topic.id")
    List<CountByTopic> countQuestionsByTopicInCourse(@Param("courseId") Long courseId);

    @Query("select q.topic.course.id as courseId, count(q) as total from Question q group by q.topic.course.id")
    List<CountByCourse> countQuestionsByCourse();

    @Query("select q.id as questionId, o.id as optionId from Question q join q.options o "
            + "where q.topic.id = :topicId and o.correct = true")
    List<QuestionOption> findCorrectOptions(@Param("topicId") Long topicId);

    @Query("select q.id as questionId, o.id as optionId from Question q join q.options o "
            + "where q.topic.id = :topicId and o.correct = false")
    List<QuestionOption> findIncorrectOptions(@Param("topicId") Long topicId);

    interface QuestionOption {
        Long getQuestionId();

        Long getOptionId();
    }

    interface CountByTopic {
        Long getTopicId();

        Long getTotal();
    }

    interface CountByCourse {
        Long getCourseId();

        Long getTotal();
    }
}
