package com.jobplatform.repositories;

import com.jobplatform.models.Cv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CvRepository extends JpaRepository<Cv, Long> {
    public List<Cv> findByUserId(Long userId);
}
