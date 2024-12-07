package com.jobplatform.repositories;

import com.jobplatform.models.Application;
import com.jobplatform.models.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount,Long>, JpaSpecificationExecutor<UserAccount> {
    Optional<UserAccount> findByEmail(String email);
    Optional<UserAccount> findByResetPasswordToken(String token);
    long countByRoleAndCreatedAtBetween(UserAccount.Role role, LocalDateTime startDate, LocalDateTime endDate);
    @Modifying
    @Transactional
    @Query("UPDATE UserAccount ua SET ua.availableJobPosts = :availableJobPosts WHERE ua.id = :userId")
    void updateAvailableJobPosts(@Param("availableJobPosts") Integer availableJobPosts, @Param("userId") Long userId);

}
