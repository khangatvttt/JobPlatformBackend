package com.jobplatform.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import javax.imageio.ImageIO;
import java.time.LocalDateTime;

@Entity
@Table(name = "cvs")
@Data
public class Cv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String jobPosition;

    @Column(nullable = false)
    @NotBlank
    private String fullName;

    @Pattern(regexp = "^\\d+$", message = "Phone number must contain only digits")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;

    @Column(nullable = false)
    @NotBlank
    @Email(message = "Invalid email format")
    private String email;

    @Column
    private String address;

    @Column(columnDefinition = "TEXT")
    private String education;

    @Column(columnDefinition = "TEXT")
    private String workExperience;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String certifications;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String imageCV;

    private String languageSkill;

    private String hobby;

    private String portfolio;

    private Boolean status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "user_id")
    @JsonIgnore
    private UserAccount user;
}

