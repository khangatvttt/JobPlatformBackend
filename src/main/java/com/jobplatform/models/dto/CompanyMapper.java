package com.jobplatform.models.dto;

import com.jobplatform.models.Company;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    Company toEntity(CompanyDto companyDto);

    CompanyDto toDto(Company company);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCompany(CompanyDto sourceCompany, @MappingTarget Company targetCompany);
}

