package com.jobplatform.models.dto;

import com.jobplatform.models.Cv;

public record CvScore(
        Cv cv,
        Double score
) {
}
