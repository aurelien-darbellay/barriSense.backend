package com.barrisense.backend.feedback.controller;

import com.barrisense.backend.feedback.dto.NumFeedBackPerHoodDto;
import com.barrisense.backend.feedback.entity.Feedback;
import com.barrisense.backend.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping("/public")
    public ResponseEntity<List<Feedback>> getAllFeedbacks() {
        return ResponseEntity.ok(feedbackService.getAll());
    }

    @GetMapping("/public/feedbacks-by-hood")
    public ResponseEntity<List<NumFeedBackPerHoodDto>> getNumFeedbacksByHood() {
        return ResponseEntity.ok(feedbackService.getNumFeedbacksByHood());
    }

    @PostMapping("/protected/new")
    public ResponseEntity<Feedback> addFeedback(@Valid @RequestBody Feedback feedback) {
        Feedback saved = feedbackService.addFeedback(feedback);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
