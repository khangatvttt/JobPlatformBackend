package com.jobplatform.models.dto;

import com.jobplatform.models.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;

public record UserDto(
        Long id,

        @NotBlank
        @Email(message = "Invalid email format")
        String email,

        @NotBlank
        @Size(min = 6, message = "Password must have at least 6 characters")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "Password must contain at least one uppercase letter, one lowercase letter, and one number")
        String password,

        @NotBlank
        @NotBlank
        String fullName,

        @NotBlank
        @Pattern(regexp = "^\\d+$", message = "Phone number must contain only digits")
        @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
        String phone,

        Long companyId,

        String companyName,

        String businessLicense,

        Boolean isActive,

        Boolean isNonLocked,

        @NotNull
        @Enumerated(EnumType.STRING)
        UserAccount.Role role
        ) {
}
