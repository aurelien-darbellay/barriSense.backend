package com.barrisense.backend.feedback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class NumFeedBackPerHoodDto {
    private final Integer hoodId;
    private Long numFeedbacks;
}
