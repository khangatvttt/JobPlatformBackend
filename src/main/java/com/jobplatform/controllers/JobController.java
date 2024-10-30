package com.jobplatform.controllers;

import com.jobplatform.models.dto.JobDetailDto;
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
    public ResponseEntity<JobDetailDto> addJob(@RequestBody JobDetailDto jobDetailDto) {
        JobDetailDto addJob = jobService.addJob(jobDetailDto);
        return new ResponseEntity<>(addJob, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<List<JobDetailDto>> findAllJobs(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(required = false) String title,
                                                          @RequestParam(required = false) Boolean related) {
        Pageable pageable = PageRequest.of(page, size);
        List<JobDetailDto> jobs = jobService.findAllJobs(pageable, title, related);
        return new ResponseEntity<>(jobs, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDetailDto> findJobById(@PathVariable Long id) {
        JobDetailDto job = jobService.findJobById(id);
        return new ResponseEntity<>(job, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobDetailDto> updateJob(@PathVariable Long id, @Valid @RequestBody JobDetailDto jobDetailDto) {
        JobDetailDto updatedJob = jobService.updateJob(id, jobDetailDto);
        return new ResponseEntity<>(updatedJob, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return new ResponseEntity<>("Job deleted successfully", HttpStatus.OK);
    }

}
