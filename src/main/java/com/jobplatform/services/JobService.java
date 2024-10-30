package com.jobplatform.services;

import com.jobplatform.models.Company;
import com.jobplatform.models.Job;
import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.JobDto;
import com.jobplatform.repositories.CompanyRepository;
import com.jobplatform.repositories.JobRepository;
import com.jobplatform.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

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
    public JobDto addJob(JobDto jobDto){
        Job job=new Job();
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long companyId=getCompanyIdForUser(userAccount);

        job.setTitle(jobDto.title());
        job.setDescription(jobDto.description());
        job.setWorkExperience(jobDto.workExperience());
        job.setBenefits(jobDto.benefits());
        job.setSalary(jobDto.salary());
        job.setDeadline(jobDto.deadline());

        job.setCreateAt(LocalDateTime.now());

        job.setUser(userAccount);

        Job savedJob = jobRepository.save(job);

        return convertToDto(savedJob);
    }
    public Page<JobDto> findAllJobs(Pageable pageable){
        Page<Job> jobs= jobRepository.findAll(pageable);
        return jobs.map(this::convertToDto);
    }

    public JobDto findJobById(Long id){
        Job job= jobRepository.findById(id).orElseThrow(()->new RuntimeException("Job not found with id:" + id));
        return convertToDto(job);
    }

    public JobDto updateJob(Long id, JobDto jobDto){
        Job job=jobRepository.findById(id).orElseThrow(()->new RuntimeException("Job not found with id:" + id));
        job.setTitle(jobDto.title());
        job.setDescription(jobDto.description());
        job.setWorkExperience(jobDto.workExperience());
        job.setBenefits(jobDto.benefits());
        job.setSalary(jobDto.salary());
        job.setDeadline(jobDto.deadline());
        job.setCreateAt(LocalDateTime.now());


        Job updatedJob=jobRepository.save(job);
        return convertToDto(updatedJob);
    }

    @Transactional
    private Long getCompanyIdForUser(UserAccount userAccount){
        if(userAccount.getCompany()==null){
            List<Company> defaultCompanies=companyRepository.findByName("Freelancer");

            if(defaultCompanies.isEmpty()){
                throw new RuntimeException("Default company 'Freelancer' not found");
            }
            Company defaultCompany=defaultCompanies.get(0);
            return defaultCompany.getId();
        }else{
            return userAccount.getCompany().getId();
        }
    }
    public void deleteJob(Long id){
        if(!jobRepository.existsById(id)){
            throw new RuntimeException("Job not found with id: " +id);
        }
        jobRepository.deleteById(id);
    }
    private JobDto convertToDto(Job job) {
        return new JobDto(
                job.getTitle(),
                job.getDescription(),
                job.getWorkExperience(),
                job.getBenefits(),
                job.getUser().getCompany(),
                job.getSalary(),
                job.getDeadline(),
                job.getCreateAt()
        );
    }

}
