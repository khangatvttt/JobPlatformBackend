package com.jobplatform.services;

import com.jobplatform.models.Job;
import com.jobplatform.models.JobSave;
import com.jobplatform.models.UserAccount;
import com.jobplatform.repositories.JobRepository;
import com.jobplatform.repositories.JobSaveRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

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
                .orElseThrow(()-> new NoSuchElementException("Job not found"));
        JobSave jobSave=new JobSave();
        jobSave.setUser(userAccount);
        jobSave.setJob(job);
        jobSave.setSavedAt(LocalDateTime.now());
        return jobSaveRepository.save(jobSave);
    }

    public void deleteJobSave(Long jobId){
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NoSuchElementException("Job not found"));

        JobSave jobSave = jobSaveRepository.findByUserAndJob(userAccount, job)
                .orElseThrow(() -> new NoSuchElementException("JobSave not found for this user and job"));

        jobSaveRepository.delete(jobSave);
    }

    public List<JobSave> findJobSaveByUser(){
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jobSaveRepository.findByUser(userAccount);
    }


}