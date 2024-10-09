package com.jobplatform.models;

import jakarta.persistence.*;
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
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column
    private String workExperience;

    @Column
    private String benefits;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private Double salary;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(nullable = false)
    private LocalDateTime createAt;

    @OneToMany (mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Application> applications;

    @OneToMany (mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Review> reviews;

    @OneToMany (mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<JobSave> jobSaves;

    @ManyToOne
    @JoinColumn (name = "user_id")
    private UserAccount user;
}
