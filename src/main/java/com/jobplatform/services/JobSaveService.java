package com.jobplatform.services;

import com.jobplatform.models.Job;
import com.jobplatform.models.JobSave;
import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.JobSaveDto;
import com.jobplatform.repositories.JobRepository;
import com.jobplatform.repositories.JobSaveRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class JobSaveService {

    private final JobSaveRepository jobSaveRepository;
    private final JobRepository jobRepository;

    public JobSaveService(JobSaveRepository jobSaveRepository, JobRepository jobRepository) {
        this.jobSaveRepository = jobSaveRepository;
        this.jobRepository = jobRepository;
    }

    public JobSave addJobSave(Long jobId){
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Job job= jobRepository.findById(jobId)
                .orElseThrow(()-> new NoSuchElementException("Job with id "+jobId+" is not found"));
        JobSave jobSave=new JobSave();
        jobSave.setUser(userAccount);
        jobSave.setJob(job);
        jobSave.setSavedAt(LocalDateTime.now());
        return jobSaveRepository.save(jobSave);
    }

    public void deleteJobSave(Long jobId){
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NoSuchElementException("Job with id "+jobId+" is not found"));

        JobSave jobSave = jobSaveRepository.findByUserAndJob(userAccount, job)
                .orElseThrow(() -> new NoSuchElementException("User with id "+userAccount.getId()+" hasn't saved Job with id "+jobId+" yet"));

        jobSaveRepository.delete(jobSave);
    }

    public List<JobSaveDto> findJobSaveByUser() {
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<JobSave> jobSaves = jobSaveRepository.findByUser(userAccount);

        return jobSaves.stream()
                .map(jobSave -> {
                    Job job = jobSave.getJob();
                    UserAccount user = jobSave.getUser();

                    return new JobSaveDto(
                            jobSave.getId(),
                            jobSave.getSavedAt(),
                            jobSave.getUser().getId(),
                            job.getId()
                    );
                })
                .collect(Collectors.toList());
    }


}
