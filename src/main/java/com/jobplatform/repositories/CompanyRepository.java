package com.jobplatform.repositories;

import com.jobplatform.models.Company;
import org.checkerframework.checker.units.qual.C;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company,Long>, JpaSpecificationExecutor<Company> {
    List<Company> findByName(String name);
}
