package com.jobplatform.controllers;

import com.jobplatform.models.Job;
import com.jobplatform.models.dto.JobDto;
import com.jobplatform.services.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("")
    public ResponseEntity<JobDto> addJob(@RequestBody JobDto jobDto){
        JobDto addJob = jobService.addJob(jobDto);
        return new ResponseEntity<>(addJob, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<Page<JobDto>> findAllJobs( @RequestParam(defaultValue = "0") int page, @RequestParam(required = false) Integer size) {
        int pageSize=(size!=null)?size:10;
        Pageable pageable= PageRequest.of(page, pageSize);
        Page<JobDto> jobs = jobService.findAllJobs(pageable);
        return new ResponseEntity<>(jobs, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDto> findJobById(@PathVariable Long id) {
        JobDto job = jobService.findJobById(id);
        return new ResponseEntity<>(job, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobDto> updateJob(@PathVariable Long id, @Valid @RequestBody JobDto jobDto) {
        JobDto updatedJob = jobService.updateJob(id, jobDto);
        return new ResponseEntity<>(updatedJob, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return new ResponseEntity<>("Job deleted successfully", HttpStatus.OK);
    }
}
