package com.jobplatform.services;

import com.jobplatform.models.Company;
import com.jobplatform.models.Job;
import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.*;
import com.jobplatform.repositories.CompanyRepository;
import com.jobplatform.repositories.JobRepository;
import jakarta.persistence.NonUniqueResultException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    public CompanyService(CompanyRepository companyRepository, CompanyMapper companyMapper, JobRepository jobRepository, JobMapper jobMapper) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
        this.jobRepository = jobRepository;
        this.jobMapper = jobMapper;
    }

    public CompanyDto addCompany(CompanyDto companyDto) {
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Company company = companyMapper.toEntity(companyDto);

        company.setStatus(userAccount.getRole() == UserAccount.Role.ROLE_ADMIN);
        Company savedCompany = companyRepository.save(company);
        return companyMapper.toDto(savedCompany);
    }

    public List<CompanyDto> findAllCompanies(String companyName, Boolean status) {
        List<Company> companyList=companyRepository.findAll(filterCompany(companyName,status));
        return companyList.stream().map(companyMapper::toDto).toList();
    }

    public CompanyDto findCompanyById(Long id) {
        Company company = companyRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Company with id "+id+ " is not found"));
        return companyMapper.toDto(company);
    }

    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Company with id "+id+ " is not found"));
        companyRepository.delete(company);
    }

    public CompanyDto updateCompany(Long id, CompanyDto companyDto) {
        Company company = companyRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Company not found"));

        companyMapper.updateCompany(companyDto, company);

        Company updatedCompany = companyRepository.save(company);
        return companyMapper.toDto(updatedCompany);
    }

    public Page<Job> getJobByCompany(Long companyId, int page, int size){
        companyRepository.findById(companyId).orElseThrow(() -> new NoSuchElementException("Company not found"));
        Pageable pageable = PageRequest.of(page, size);
        return jobRepository.findByUser_Company_Id(companyId, pageable);
    }

    public static Specification<Company> filterCompany(String companyName, Boolean status) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();

            if (status != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), status));
            }
            if (companyName != null && !companyName.isEmpty()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + companyName.toLowerCase() + "%"));
            }
            return predicate;
        };
    }

}
