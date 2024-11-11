package com.jobplatform.repositories;

import com.jobplatform.models.Job;
import com.jobplatform.models.JobSave;
import com.jobplatform.models.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobSaveRepository extends JpaRepository<JobSave, Long> {
    Optional<JobSave> findByUserAndJob(UserAccount user, Job job);
    List<JobSave> findByUser(UserAccount user);
}
