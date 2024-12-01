package com.jobplatform.repositories;

import com.jobplatform.models.Application;
import com.jobplatform.models.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long>, JpaSpecificationExecutor<Application> {
    long countByAppliedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    @Query("SELECT j.industry, COUNT(a) AS applicationCount " +
            "FROM Application a " +
            "JOIN a.job j " +
            "WHERE a.appliedAt BETWEEN :startDate AND :endDate " +
            "GROUP BY j.industry " +
            "ORDER BY applicationCount DESC")
    List<Object[]> findTop5IndustriesByApplications(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a.status, COUNT(a) " +
            "FROM Application a " +
            "WHERE a.appliedAt BETWEEN :startDate AND :endDate " +
            "GROUP BY a.status")
    List<Object[]> countApplicationsByStatus(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

}
