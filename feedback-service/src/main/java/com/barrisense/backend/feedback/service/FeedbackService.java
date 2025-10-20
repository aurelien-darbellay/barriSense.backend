package com.barrisense.backend.feedback.service;

import com.barrisense.backend.feedback.dto.NumFeedBackPerHoodDto;
import com.barrisense.backend.feedback.entity.Feedback;
import com.barrisense.backend.feedback.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final Mappers mappers;

    public List<Feedback> getAll() {
        return feedbackRepository.findAll();
    }

    public List<NumFeedBackPerHoodDto> getNumFeedbacksByHood() {
        return mappers.mapAllFeedbacksToNumFeedBackPerHood(feedbackRepository.findAll());
    }

    public Feedback addFeedback(Feedback feedback) {
        feedback.setCreatedAt(LocalDateTime.now());
        return feedbackRepository.save(feedback);
    }
}
