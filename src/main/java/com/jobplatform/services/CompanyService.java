package com.jobplatform.services;

import com.jobplatform.models.Company;
import com.jobplatform.models.dto.CompanyDto;
import com.jobplatform.repositories.CompanyRepository;
import jakarta.persistence.NonUniqueResultException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }
    public CompanyDto addCompany(CompanyDto companyDto) {
        List<Company> existingCompanies = companyRepository.findByName(companyDto.name());

        if (existingCompanies.size() > 1) {
            throw new NonUniqueResultException("A company with the name '" + companyDto.name() + "' already exists.");
        }

        Company company = new Company();
        company.setName(companyDto.name());
        company.setLocation(companyDto.location());
        company.setImages(companyDto.images());
        company.setDescription(companyDto.description());
        company.setWebsite(companyDto.website());
        company.setIndustry(companyDto.industry());
        company.setCompanySize(companyDto.companySize());


        Company savedCompany = companyRepository.save(company);
        return convertToDto(savedCompany);
    }

    public List<Company> findAllCompanies(){
        return companyRepository.findAll();
    }

    public List<Company> findCompanyByName(String name){
        return companyRepository.findByName(name);
    }

    public CompanyDto findCompanyById(Long id){
        Company company = companyRepository.findById(id).orElseThrow(()->new IllegalArgumentException("Company not found"));
        return convertToDto(company);
    }

    public void deleteCompany(Long id){
        companyRepository.deleteById(id);
    }

    public CompanyDto updateCompany(Long id, CompanyDto companyDto) {
        Company company = companyRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Company not found"));

        company.setName(companyDto.name());
        company.setLocation(companyDto.location());
        company.setImages(companyDto.images());
        company.setDescription(companyDto.description());
        company.setWebsite(companyDto.website());
        company.setIndustry(companyDto.industry());
        company.setCompanySize(companyDto.companySize());

        Company updatedCompany = companyRepository.save(company);
        return convertToDto(updatedCompany);
    }

    private CompanyDto convertToDto(Company company) {
        return new CompanyDto(
                company.getName(),
                company.getLocation(),
                company.getImages(),
                company.getDescription(),
                company.getWebsite(),
                company.getIndustry(),
                company.getCompanySize()
        );
    }
}
