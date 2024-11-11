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
    public Page<Job> findAllJobs(Pageable pageable, String title, Boolean related) {
        if (title != null) {
            title = title.replace('-', ' ');
        }
        return jobRepository.findAll(jobFilter(title, related), pageable);
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
    public static Specification<Job> jobFilter(String title, Boolean related) {
        related = related != null && related;
        if (related) {
            return (root, query, criteriaBuilder) -> {
                if (title == null || title.isEmpty()) {
                    return criteriaBuilder.conjunction(); // Return all jobs if no title is provided
                }

                String[] keywords = title.split(" ");
                List<Predicate> predicates = new ArrayList<>();

                for (String keyword : keywords) {
                    predicates.add(criteriaBuilder.like(root.get("title"), "%" + keyword + "%"));
                }

                return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
            };
        } else {
            return (root, query, criteriaBuilder) ->
                    title == null ? null : criteriaBuilder.like(root.get("title"), "%" + title + "%");
        }
    }


    @SneakyThrows
    private void checkOwnership(Long resourceOwnerId){
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userAccount.getRole()!= UserAccount.Role.ROLE_ADMIN && !userAccount.getId().equals(resourceOwnerId)){
            throw new NoPermissionException();
        }
    }

}
