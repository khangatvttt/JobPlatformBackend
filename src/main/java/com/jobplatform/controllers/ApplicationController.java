package com.jobplatform.controllers;

import com.jobplatform.models.Application;
import com.jobplatform.models.dto.ApplicationDto;
import com.jobplatform.services.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("")
    public ResponseEntity<ApplicationDto> addApplication(@Valid @RequestBody ApplicationDto applicationDto) {
        ApplicationDto savedApplication = applicationService.addApplication(applicationDto);
        return new ResponseEntity<>(savedApplication, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<List<ApplicationDto>> findAllApplications(@RequestParam(required = false) String status,
                                                                    @RequestParam(required = false) Long jobId,
                                                                    @RequestParam(required = false) String email,
                                                                    @RequestParam(required = false) String phone,
                                                                    @RequestParam(required = false) String name,
                                                                    @RequestParam(required = false) Long recruiterId) {
        List<ApplicationDto> applications = applicationService.findAllApplications(jobId, status, name, phone, email, recruiterId);
        return new ResponseEntity<>(applications, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Application> findApplicationById(@PathVariable Long id) {
        Application application = applicationService.findApplicationById(id);
        return new ResponseEntity<>(application, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return new ResponseEntity<>("Application deleted successfully", HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApplicationDto> updateStatusApplication(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        ApplicationDto application = applicationService.updateStatusApplication(id, status);
        return new ResponseEntity<>(application, HttpStatus.OK);
    }
}

