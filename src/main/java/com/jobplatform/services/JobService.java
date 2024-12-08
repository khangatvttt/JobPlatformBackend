package com.jobplatform.services;

import com.jobplatform.models.Company;
import com.jobplatform.models.Job;
import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.JobDetailDto;
import com.jobplatform.models.dto.JobMapper;
import com.jobplatform.models.dto.UpdateJobDto;
import com.jobplatform.repositories.CompanyRepository;
import com.jobplatform.repositories.JobRepository;
import com.jobplatform.repositories.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.hibernate.Hibernate;
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
    @Transactional
    @SneakyThrows
    public JobDetailDto addJob(JobDetailDto jobDetailDto) {
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userAccount.getAvailableJobPosts()==null || userAccount.getAvailableJobPosts()<=0){
            throw new BadRequestException("This account hasn't had enough number of job post allowed to post");
        }


        if (userAccount.getCompany() != null) {
            Hibernate.initialize(userAccount.getCompany());
        }


        Job job = jobMapper.toEntity(jobDetailDto);

        job.setCreateAt(LocalDateTime.now());
        job.setUser(userAccount);
        job.setStatus(Job.Status.PENDING_APPROVAL);

        Job savedJob = jobRepository.save(job);

        //Reduce number of jobs user can post
        Integer jobPostsLeft = userAccount.getAvailableJobPosts()-1;
        userRepository.updateAvailableJobPosts(jobPostsLeft, userAccount.getId());
        userAccount.setAvailableJobPosts(jobPostsLeft);

        return jobMapper.toDto(savedJob);
    }

    // Get all jobs
    public Page<Job> findAllJobs(Pageable pageable, String title, Boolean related, String status, Long userId, String industry, String address) {
        // Check the status
        if (status!=null) {
            Job.Status.valueOf(status);
        }
        // Check the title
        if (title != null) {
            title = title.replace('-', ' ');
        }
        return jobRepository.findAll(jobFilter(title, related, status, userId, industry, address), pageable);
    }

    // Get a job by Id
    public JobDetailDto findJobById(Long id) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Job not found with id:" + id));
        return jobMapper.toDto(job);
    }

    // Update Job (partial update)
    public JobDetailDto updateJob(Long id, UpdateJobDto jobDetailDto) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Job with id: " + id +" is not found"));
        checkOwnership(job.getUser().getId());

        Job.Status oldStatus = job.getStatus();

        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        jobMapper.updateJob(jobDetailDto, job);

        if (oldStatus == Job.Status.PENDING_APPROVAL && userAccount.getRole()!= UserAccount.Role.ROLE_ADMIN){
            job.setStatus(oldStatus);
        }
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
    public static Specification<Job> jobFilter(String title, Boolean related, String status, Long userId, String industry, String address) {
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

            if (userId != null) {
                Join<Job, UserAccount> userJoin = root.join("user");
                predicates.add(criteriaBuilder.equal(userJoin.get("id"), userId));
            }

            if (industry != null) {
                predicates.add(criteriaBuilder.equal(root.get("industry"), industry));
            }

            if (address != null) {
                predicates.add(criteriaBuilder.equal(root.get("address"), address));
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
