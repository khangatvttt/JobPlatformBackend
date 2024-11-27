package com.jobplatform.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Data
public class Application {

    public enum CVType{
        UPLOADED_CV,
        CREATED_CV
    }

    public enum Status{
        PENDING,
        REJECTED,
        ACCEPTED,
        INTERVIEWING
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime appliedAt;

    @NotNull
    private Long cvId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CVType cvType;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "application")
    private InterviewInvitation interviewInvitation;
}

