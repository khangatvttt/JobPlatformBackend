package com.jobplatform.repositories;

import com.jobplatform.models.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount,Long> {
    Optional<UserAccount> findByEmail(String email);
    Optional<UserAccount> findByResetPasswordToken(String token);
}
