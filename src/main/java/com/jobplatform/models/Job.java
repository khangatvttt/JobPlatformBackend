package com.jobplatform.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "jobs")
@Data
public class Job {

    public enum Status{
        PENDING_APPROVAL,
        SHOW,
        HIDE,
        DISQUALIFIED
    }

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

    private String address;

    @Column(nullable = false)
    @NotNull(message = "Salary is required.")
    @Min(value = 0, message = "Salary must be positive.")
    private Double salary;

    @Column(nullable = false)
    @NotNull(message = "Application deadline is required.")
    @Future(message = "Deadline must be a future date.")
    private LocalDateTime deadline;

    private LocalDateTime createAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    private String level;

    private String workType;

    private Integer numberOfRecruits;

    @JsonIgnore
    @OneToMany (mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Application> applications;

    @JsonIgnore
    @OneToMany (mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Review> reviews;

    @JsonIgnore
    @OneToMany (mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<JobSave> jobSaves;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "user_id")
    @NotNull(message = "User account is required.")
    private UserAccount user;

}
