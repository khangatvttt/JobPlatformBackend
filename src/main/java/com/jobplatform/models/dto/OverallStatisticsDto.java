package com.jobplatform.models.dto;

public record OverallStatisticsDto(
        long numberOfJobs,
        long numberOfApplications,
        long numberOfNewMembers
) {
}