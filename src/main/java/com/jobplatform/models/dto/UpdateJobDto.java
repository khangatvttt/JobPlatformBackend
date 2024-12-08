package com.jobplatform.models.dto;

import com.jobplatform.models.Job;

import java.time.LocalDateTime;

public record UpdateJobDto(
        String level,

        String workType,

        Integer numberOfRecruits,

        Job.Status status,

        LocalDateTime deadline
        ) {
}
