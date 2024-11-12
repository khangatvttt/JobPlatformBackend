package com.jobplatform.services;

import com.jobplatform.models.Company;
import com.jobplatform.models.dto.CompanyDto;
import com.jobplatform.models.dto.CompanyMapper;
import com.jobplatform.repositories.CompanyRepository;
import jakarta.persistence.NonUniqueResultException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public CompanyService(CompanyRepository companyRepository, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
    }

    public CompanyDto addCompany(CompanyDto companyDto) {

        Company company = companyMapper.toEntity(companyDto);

        Company savedCompany = companyRepository.save(company);
        return companyMapper.toDto(savedCompany);
    }

    public List<CompanyDto> findAllCompanies(String companyName) {
        List<Company> companyList;
        if (companyName != null) {
            companyList = companyRepository.findByName(companyName);
        } else {
            companyList = companyRepository.findAll();
        }
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

}
