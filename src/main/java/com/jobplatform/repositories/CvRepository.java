package com.jobplatform.repositories;

import com.jobplatform.models.Cv;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;


public interface CvRepository extends JpaRepository<Cv, Long>, PagingAndSortingRepository<Cv, Long> {
    Page<Cv> findByUserId(Long userId, Pageable pageable);
}
