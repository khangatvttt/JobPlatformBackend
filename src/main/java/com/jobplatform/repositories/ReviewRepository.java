package com.jobplatform.repositories;

import com.jobplatform.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {

    boolean existsByUserIdAndJobId(Long userId, Long jobId);
}
