package com.jobplatform.models;

import jakarta.persistence.*;
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
    private String name;

    @Column
    private String location;

    @Column
    private String images;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String website;

    @Column
    private String industry;

    @Column
    private String companySize;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Set<UserAccount> users;
}

