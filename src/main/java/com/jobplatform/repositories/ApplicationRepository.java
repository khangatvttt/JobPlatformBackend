package com.jobplatform.repositories;

import com.jobplatform.models.Application;
import com.jobplatform.models.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long>, JpaSpecificationExecutor<Application> {

    List<Application> findByJob(Job job);

    List<Application> findByStatus(Application.Status applicationStatus);
}
