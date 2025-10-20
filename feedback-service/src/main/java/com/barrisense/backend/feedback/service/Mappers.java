package com.barrisense.backend.feedback.service;

import com.barrisense.backend.feedback.dto.NumFeedBackPerHoodDto;
import com.barrisense.backend.feedback.entity.Feedback;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class Mappers {
    public List<NumFeedBackPerHoodDto> mapAllFeedbacksToNumFeedBackPerHood(List<Feedback> feedbacks) {
        List<NumFeedBackPerHoodDto> listFeedBacksPerHood = new ArrayList<>();
        return feedbacks.stream()
                .reduce(listFeedBacksPerHood, this::createNewMapOrUpdateExisting, (l1, l2) -> l1);
    }

    private List<NumFeedBackPerHoodDto> createNewMapOrUpdateExisting(List<NumFeedBackPerHoodDto> listFeedbacksPerHood, Feedback newFeedback) {
        NumFeedBackPerHoodDto target = listFeedbacksPerHood.stream()
                .filter(item -> Objects.equals(item.getHoodId(), newFeedback.getHoodId()))
                .findFirst()
                .orElse(null);
        if (target == null) listFeedbacksPerHood.add(NumFeedBackPerHoodDto.builder()
                .hoodId(newFeedback.getHoodId())
                .numFeedbacks(1L).build());
        else target.setNumFeedbacks(target.getNumFeedbacks() + 1);
        return listFeedbacksPerHood;
    }
}
