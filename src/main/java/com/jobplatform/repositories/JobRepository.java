package com.jobplatform.repositories;

import com.jobplatform.models.Company;
import com.jobplatform.models.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    Page<Job> findByUser_Company_Id(Long companyId, Pageable pageable);
    List<Job> findByUserId(Long userId);

    long countByCreateAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    @Query("SELECT j.industry, COUNT(j) AS jobCount " +
            "FROM Job j " +
            "WHERE j.createAt BETWEEN :startDate AND :endDate " +
            "GROUP BY j.industry " +
            "ORDER BY jobCount DESC")
    List<Object[]> findTop5IndustriesBetweenDates(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);
}
