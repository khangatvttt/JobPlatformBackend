package com.jobplatform.services;

import com.jobplatform.models.*;
import com.jobplatform.models.dto.ApplicationDto;
import com.jobplatform.models.dto.ApplicationMapper;
import com.jobplatform.repositories.ApplicationRepository;
import com.jobplatform.repositories.CvFileRepository;
import com.jobplatform.repositories.CvRepository;
import com.jobplatform.repositories.JobRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.SneakyThrows;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.naming.NoPermissionException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final ApplicationMapper applicationMapper;
    private final CvFileRepository cvFileRepository;
    private final CvRepository cvRepository;

    public ApplicationService(ApplicationRepository applicationRepository, JobRepository jobRepository, ApplicationMapper applicationMapper, CvFileRepository cvFileRepository, CvRepository cvRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.applicationMapper = applicationMapper;
        this.cvFileRepository = cvFileRepository;
        this.cvRepository = cvRepository;
    }

    @SneakyThrows
    public ApplicationDto addApplication(ApplicationDto applicationDto) {
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Job job = jobRepository.findById(applicationDto.jobId()).orElseThrow(() -> new NoSuchElementException("Not found"));

        if (Objects.equals(applicationDto.cvType(), String.valueOf(Application.CVType.UPLOADED_CV))) {
            CvFile cvFile = cvFileRepository.findById(applicationDto.cvId()).orElseThrow(() -> new NoSuchElementException("Cv not found"));
            List<CvFile> cvFiles = cvFileRepository.findByUser(userAccount);
            if (!cvFiles.contains(cvFile)) {
                throw new NoPermissionException();
            }
        }

        if (Objects.equals(applicationDto.cvType(), String.valueOf(Application.CVType.CREATED_CV))) {
            Cv cv = cvRepository.findById(applicationDto.cvId()).orElseThrow(() -> new NoSuchElementException("Cv not found"));
            Set<Cv> cvs = userAccount.getCvs();
            if (!cvs.contains(cv)) {
                throw new NoPermissionException();
            }
        }

        Application application = applicationMapper.toEntity(applicationDto);
        application.setUser(userAccount);
        application.setAppliedAt(LocalDateTime.now());
        application.setJob(job);

        return applicationMapper.toDto(applicationRepository.save(application));
    }

    public List<ApplicationDto> findAllApplications(Long jobId, String status, String name, String phone, String email) {
        Application.Status applicationStatus = status != null ? Application.Status.valueOf(status) : null;
        return applicationRepository.findAll(filterApplication(name, email, phone, jobId, applicationStatus))
                .stream()
                .map(applicationMapper::toDto)
                .toList();
    }

    public Application findApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Application with id " + id + " is not found"));
    }

    public void deleteApplication(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Application with id " + id + " is not found"));
        applicationRepository.delete(application);
    }

    public ApplicationDto updateStatusApplication(Long id, String status) {
        Application.Status statusApplication = Application.Status.valueOf(status);

        Application existingApplication = applicationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Application with id " + id + " is not found"));

        existingApplication.setStatus(statusApplication);

        return applicationMapper.toDto(applicationRepository.save(existingApplication));
    }


    public static Specification<Application> filterApplication(
            String userName,
            String email,
            String phone,
            Long jobId,
            Application.Status status
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();


            // Status filter
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (jobId != null) {
                Join<Application, Job> jobJoin = root.join("job");
                predicates.add(criteriaBuilder.equal(jobJoin.get("id"), jobId));
            }

            if (userName != null) {
                Join<Application, UserAccount> userJoin = root.join("user");
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("fullName")), "%" + userName.toLowerCase() + "%"));
            }

            if (email != null) {
                Join<Application, UserAccount> userJoin = root.join("user");
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("email")), "%" + email.toLowerCase() + "%"));
            }

            if (phone != null) {
                Join<Application, UserAccount> userJoin = root.join("user");
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("phone")), "%" + phone.toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }


}