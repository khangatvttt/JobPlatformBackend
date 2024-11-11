package com.jobplatform.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Entity
@Table(name = "companies")
@Data
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    private String name;

    @Column
    private String location;

    @Column
    @Pattern(regexp = "^(https?|ftp|file)://.+$", message = "Invalid URL format for image")
    private String images;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    @Pattern(regexp = "^(https?|ftp)://.+$", message = "Invalid website URL")
    private String website;

    @Column
    @NotBlank(message = "Industry is required")
    private String industry;

    @Column
    private Integer companySize;

    @JsonIgnore
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Set<UserAccount> users;
}

