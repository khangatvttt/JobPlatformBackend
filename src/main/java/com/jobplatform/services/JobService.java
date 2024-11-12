package com.jobplatform.services;

import com.jobplatform.models.Company;
import com.jobplatform.models.Job;
import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.JobDetailDto;
import com.jobplatform.models.dto.JobMapper;
import com.jobplatform.repositories.CompanyRepository;
import com.jobplatform.repositories.JobRepository;
import com.jobplatform.repositories.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.NoPermissionException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class JobService {
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobMapper jobMapper;

    public JobService(JobRepository jobRepository, UserRepository userRepository, CompanyRepository companyRepository, JobMapper jobMapper) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.jobMapper = jobMapper;
    }

    // Create a new job
    public JobDetailDto addJob(JobDetailDto jobDetailDto) {
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Job job = jobMapper.toEntity(jobDetailDto);

        job.setCreateAt(LocalDateTime.now());
        job.setUser(userAccount);

        Job savedJob = jobRepository.save(job);

        return jobMapper.toDto(savedJob);
    }

    // Get all jobs
    public Page<Job> findAllJobs(Pageable pageable, String title, Boolean related, String status) {
        // Check the status
        Job.Status.valueOf(status);
        // Check the title
        if (title != null) {
            title = title.replace('-', ' ');
        }
        return jobRepository.findAll(jobFilter(title, related, status), pageable);
    }

    // Get a job by Id
    public JobDetailDto findJobById(Long id) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Job not found with id:" + id));
        return jobMapper.toDto(job);
    }

    // Update Job (partial update)
    public JobDetailDto updateJob(Long id, JobDetailDto jobDetailDto) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Job with id: " + id +" is not found"));
        checkOwnership(job.getUser().getId());

        jobMapper.updateJob(jobDetailDto, job);
        Job updatedJob = jobRepository.save(job);
        return jobMapper.toDto(updatedJob);
    }

    // Delete job by Id
    public void deleteJob(Long id) {
        Job job = jobRepository.findById(id).orElseThrow(()->new NoSuchElementException("Job with id: " + id +" is not found"));
        checkOwnership(job.getUser().getId());
        jobRepository.deleteById(id);
    }

    // Search job function
    public static Specification<Job> jobFilter(String title, Boolean related, String status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Title filter
            if (title != null && !title.isEmpty()) {
                if (related) {
                    String[] keywords = title.split(" ");
                    List<Predicate> keywordPredicates = new ArrayList<>();
                    for (String keyword : keywords) {
                        keywordPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"));
                    }
                    predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new Predicate[0])));
                } else {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
                }
            }

            // Status filter
            if (status != null && !status.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }


    @SneakyThrows
    private void checkOwnership(Long resourceOwnerId){
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userAccount.getRole()!= UserAccount.Role.ROLE_ADMIN && !userAccount.getId().equals(resourceOwnerId)){
            throw new NoPermissionException();
        }
    }

}
