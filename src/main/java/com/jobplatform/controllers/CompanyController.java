package com.jobplatform.controllers;

import com.jobplatform.models.Company;
import com.jobplatform.models.dto.CompanyDto;
import com.jobplatform.models.dto.CompanyExtendedDto;
import com.jobplatform.services.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
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
    public ResponseEntity<CompanyExtendedDto> findCompanyById(@PathVariable Long id){
        CompanyExtendedDto companyDto= companyService.findCompanyById(id);
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
}

