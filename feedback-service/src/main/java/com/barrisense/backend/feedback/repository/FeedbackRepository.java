package com.barrisense.backend.feedback.repository;

import com.barrisense.backend.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
