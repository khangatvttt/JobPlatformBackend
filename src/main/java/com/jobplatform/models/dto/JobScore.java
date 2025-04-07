package com.jobplatform.models.dto;

public record JobScore(
        JobDetailDto job,
        Double score
) {
}
