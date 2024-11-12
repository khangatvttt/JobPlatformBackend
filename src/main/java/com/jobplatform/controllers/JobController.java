package com.jobplatform.controllers;

import com.jobplatform.models.Job;
import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.JobDetailDto;
import com.jobplatform.models.dto.JobMapper;
import com.jobplatform.services.JobService;
import jakarta.validation.Valid;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.naming.NoPermissionException;
import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobController {
    private final JobService jobService;
    private final JobMapper jobMapper;

    public JobController(JobService jobService, JobMapper jobMapper) {
        this.jobService = jobService;
        this.jobMapper = jobMapper;
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
                                                          @RequestParam(required = false) Boolean related,
                                                          @RequestParam(required = false) String status,
                                                          @RequestParam(required = false) Long userId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Job> jobs = jobService.findAllJobs(pageable, title, related, status, userId);
        List<JobDetailDto> listJobs = jobs.getContent().stream().map(jobMapper::toDto).toList();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Pages", String.valueOf(jobs.getTotalPages()));
        headers.add("X-Total-Elements", String.valueOf(jobs.getTotalElements()));

        return new ResponseEntity<>(listJobs, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDetailDto> findJobById(@PathVariable Long id) {
        JobDetailDto job = jobService.findJobById(id);
        return new ResponseEntity<>(job, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<JobDetailDto> updateJob(@PathVariable Long id,@RequestBody JobDetailDto jobDetailDto) {
        JobDetailDto updatedJob = jobService.updateJob(id, jobDetailDto);
        return new ResponseEntity<>(updatedJob, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
