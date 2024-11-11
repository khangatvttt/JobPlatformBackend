package com.jobplatform.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "jobs")
@Data
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull(message = "Job title is required.")
    @Size(min = 5, max = 1000, message = "Job title must be between 5 and 1000 characters")
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotNull(message = "Job description is required.")
    @Size(min = 20, message = "Job description must be at least 20 characters.")
    private String description;

    @Column(columnDefinition = "Text")
    private String workExperience;

    @Column(columnDefinition = "Text")
    private String benefits;

    private String industry;

    @Column(nullable = false)
    @NotNull(message = "Salary is required.")
    @Min(value = 0, message = "Salary must be positive.")
    private Double salary;

    @Column(nullable = false)
    @NotNull(message = "Application deadline is required.")
    @Future(message = "Deadline must be a future date.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime deadline;

    @Column(nullable = false)
    @NotNull(message = "Creation date is required.")
    @PastOrPresent(message = "Create date must be in the past or present")
    private LocalDateTime createAt;

    @OneToMany (mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Application> applications;

    @OneToMany (mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Review> reviews;

    @OneToMany (mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<JobSave> jobSaves;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "user_id")
    @NotNull(message = "User account is required.")
    private UserAccount user;

}
