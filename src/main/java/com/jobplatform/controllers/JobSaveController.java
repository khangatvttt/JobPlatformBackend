package com.jobplatform.controllers;

import com.jobplatform.models.JobSave;
import com.jobplatform.services.JobSaveService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/job-saves")
public class JobSaveController {

    private final JobSaveService jobSaveService;

    public JobSaveController(JobSaveService jobSaveService) {
        this.jobSaveService = jobSaveService;
    }

    @PostMapping("")
    public JobSave addJobSave(@RequestParam Long jobId){
        return jobSaveService.addJobSave(jobId);
    }

    @DeleteMapping("")
    public void deleteJobSave(@RequestParam Long jobId){
        jobSaveService.deleteJobSave(jobId);
    }

    @GetMapping("")
    public List<JobSave> findJobSavesByUser(){
        return jobSaveService.findJobSaveByUser();
    }
}
