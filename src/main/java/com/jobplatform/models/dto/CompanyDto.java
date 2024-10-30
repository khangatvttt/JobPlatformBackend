package com.jobplatform.models.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompanyDto(
        @NotBlank(message = "Company name is required")
        @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
        String name,

        String location,

        @Pattern(regexp = "^(https?|ftp|file)://.+$", message = "Invalid URL format for image")
        String images,

        String description,

        @Pattern(regexp = "^(https?|ftp)://.+$", message = "Invalid website URL")
        String website,

        @NotBlank(message = "Industry is required")
        String industry,

        Integer companySize
) {}
