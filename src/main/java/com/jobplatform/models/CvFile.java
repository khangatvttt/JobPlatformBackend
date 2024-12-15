package com.jobplatform.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class CvFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String cvUrl;

    private LocalDateTime uploadedAt;

    @NotNull
    private String cvName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "user_id")
    @JsonIgnore
    private UserAccount user;
}
