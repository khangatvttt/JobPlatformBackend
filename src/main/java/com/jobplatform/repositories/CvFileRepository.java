package com.jobplatform.repositories;

import com.jobplatform.models.CvFile;
import com.jobplatform.models.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CvFileRepository extends JpaRepository<CvFile, Long> {

    List<CvFile> findByUser(UserAccount user);
}
