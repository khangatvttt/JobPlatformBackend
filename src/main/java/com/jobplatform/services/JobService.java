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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class JobService {
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public JobService(JobRepository jobRepository, UserRepository userRepository, CompanyRepository companyRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public JobDetailDto addJob(JobDetailDto jobDetailDto){
        Job job = new Job();
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        job.setTitle(jobDetailDto.title());
        job.setDescription(jobDetailDto.description());
        job.setWorkExperience(jobDetailDto.workExperience());
        job.setBenefits(jobDetailDto.benefits());
        job.setSalary(jobDetailDto.salary());
        job.setDeadline(jobDetailDto.deadline());

        job.setCreateAt(LocalDateTime.now());
        job.setUser(userAccount);

        Job savedJob = jobRepository.save(job);

        return JobMapper.toJobDetailDto(savedJob);
    }
    public List<JobDetailDto> findAllJobs(Pageable pageable, String title, Boolean related){
        if (title!=null){
            title = title.replace('-',' ');
        }
        Page<Job> jobs= jobRepository.findAll(jobFilter(title, related), pageable);
        return jobs.getContent().stream().map(JobMapper::toJobDetailDto).toList();
    }

    public JobDetailDto findJobById(Long id){
        Job job= jobRepository.findById(id).orElseThrow(()->new NoSuchElementException("Job not found with id:" + id));
        return JobMapper.toJobDetailDto(job);
    }

    public JobDetailDto updateJob(Long id, JobDetailDto jobDetailDto){
        Job job=jobRepository.findById(id).orElseThrow(()->new RuntimeException("Job not found with id:" + id));
        job.setTitle(jobDetailDto.title());
        job.setDescription(jobDetailDto.description());
        job.setWorkExperience(jobDetailDto.workExperience());
        job.setBenefits(jobDetailDto.benefits());
        job.setSalary(jobDetailDto.salary());
        job.setDeadline(jobDetailDto.deadline());


        Job updatedJob=jobRepository.save(job);
        return JobMapper.toJobDetailDto(updatedJob);
    }

    public void deleteJob(Long id){
        if(!jobRepository.existsById(id)){
            throw new RuntimeException("Job not found with id: " +id);
        }
        jobRepository.deleteById(id);
    }

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
        }
        else {return (root, query, criteriaBuilder) ->
                title == null ? null : criteriaBuilder.like(root.get("title"), "%" + title + "%");
}
    }


}
