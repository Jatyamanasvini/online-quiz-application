package com.example.quizplatform.repository;

import com.example.quizplatform.domain.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    @Query("select a from QuizAttempt a join fetch a.topic t join fetch t.course "
            + "where a.user.id = :userId order by a.submittedAt desc")
    List<QuizAttempt> findAllByUserIdWithTopic(@Param("userId") Long userId);

    @Query("select a from QuizAttempt a join fetch a.topic t join fetch t.course where a.id = :id")
    Optional<QuizAttempt> findByIdWithTopic(@Param("id") Long id);

    @Query("select a from QuizAttempt a join fetch a.topic t join fetch a.user "
            + "order by a.submittedAt desc")
    List<QuizAttempt> findAllWithUserAndTopic();

    @Query("select a.topic.id as topicId, a.topic.title as topicTitle, count(a) as attempts, "
            + "avg(a.accuracy) as averageAccuracy, count(distinct a.user.id) as students "
            + "from QuizAttempt a group by a.topic.id, a.topic.title order by a.topic.id")
    List<TopicPerformance> findTopicPerformance();

    interface TopicPerformance {
        Long getTopicId();

        String getTopicTitle();

        Long getAttempts();

        Double getAverageAccuracy();

        Long getStudents();
    }
}
