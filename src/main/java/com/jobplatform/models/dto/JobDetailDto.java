package com.jobplatform.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jobplatform.models.Company;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record JobDetailDto(

        Long id,

        @NotNull(message = "Job title is required.")
        @Size(min = 5, max = 1000, message = "Job title must be between 5 and 1000 characters")
        String title,

        @NotNull(message = "Job description is required.")
        @Size(min = 20, message = "Job description must be at least 20 characters.")
        String description,

        String workExperience,

        String benefits,

        String companyName,

        String companyImages,

        String companyLocation,

        String industry,

        @NotNull(message = "Salary is required.")
        @Min(value = 0, message = "Salary must be positive.")
        Double salary,

        @NotNull(message = "Application deadline is required.")
        @Future(message = "Deadline must be a future date.")
        LocalDateTime deadline,

        LocalDateTime createAt

) {}
