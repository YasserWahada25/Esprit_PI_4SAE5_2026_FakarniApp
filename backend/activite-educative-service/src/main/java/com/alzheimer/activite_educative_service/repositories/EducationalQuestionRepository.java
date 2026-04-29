package com.alzheimer.activite_educative_service.repositories;

import com.alzheimer.activite_educative_service.entities.EducationalQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EducationalQuestionRepository extends JpaRepository<EducationalQuestion, Long> {

    List<EducationalQuestion> findByActivityIdOrderByOrderIndexAsc(Long activityId);

    long countByActivityId(Long activityId);

    boolean existsByIdAndActivityId(Long questionId, Long activityId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM EducationalQuestion q WHERE q.activity.id = :activityId")
    void deleteAllForActivity(@Param("activityId") Long activityId);
}
