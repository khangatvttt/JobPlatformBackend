package com.jobplatform.controllers;

import com.jobplatform.models.Job;
import com.jobplatform.models.dto.CompanyDto;
import com.jobplatform.models.dto.JobDetailDto;
import com.jobplatform.models.dto.JobMapper;
import com.jobplatform.services.CompanyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;
    private final JobMapper jobMapper;

    public CompanyController(CompanyService companyService, JobMapper jobMapper) {
        this.companyService = companyService;
        this.jobMapper = jobMapper;
    }

    @PostMapping("")
    public ResponseEntity<CompanyDto> addCompany(@Valid @RequestBody CompanyDto companyDto) {
        CompanyDto savedCompany = companyService.addCompany(companyDto);
        return new ResponseEntity<>(savedCompany, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<List<CompanyDto>> findAllCompanies(@RequestParam(required = false) String companyName,
                                                             @RequestParam(required = false) Boolean status){
        List<CompanyDto> companies= companyService.findAllCompanies(companyName, status);
        return new ResponseEntity<>(companies, HttpStatus.OK);
    }


    @GetMapping("/{id}")
    public ResponseEntity<CompanyDto> findCompanyById(@PathVariable Long id){
        CompanyDto companyDto= companyService.findCompanyById(id);
        return new ResponseEntity<>(companyDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCompany(@PathVariable Long id){
        companyService.deleteCompany(id);
        return new ResponseEntity<>("Company deleted successfully", HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CompanyDto> updateCompany(@PathVariable Long id, @Valid @RequestBody CompanyDto companyDto) {
        CompanyDto updatedCompany = companyService.updateCompany(id, companyDto);
        return new ResponseEntity<>(updatedCompany, HttpStatus.OK);
    }

    @GetMapping("/{id}/jobs")
    public ResponseEntity<List<JobDetailDto>> getJobByCompany(@PathVariable Long id,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size) {
        Page<Job> jobs = companyService.getJobByCompany(id, page, size);
        List<JobDetailDto> jobDetailDtoList = jobs.stream().map(jobMapper::toDto).toList();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Pages", String.valueOf(jobs.getTotalPages()));
        headers.add("X-Total-Elements", String.valueOf(jobs.getTotalElements()));
        return new ResponseEntity<>(jobDetailDtoList, headers, HttpStatus.OK);
    }
}

