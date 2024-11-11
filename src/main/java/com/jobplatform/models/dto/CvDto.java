package com.jobplatform.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record CvDto(
        String jobPosition,
        String fullName,
        @Pattern(regexp = "^\\d+$", message = "Phone number must contain only digits")
        @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
        String phone,
        @Email(message = "Invalid email format") String email,
        String address,
        String education,
        String workExperience,
        String skills,
        String certifications
) {}
