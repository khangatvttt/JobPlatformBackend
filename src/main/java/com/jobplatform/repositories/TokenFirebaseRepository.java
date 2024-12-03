package com.jobplatform.repositories;

import com.jobplatform.models.TokenFirebase;
import com.jobplatform.models.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TokenFirebaseRepository extends JpaRepository<TokenFirebase, Long> {

    Boolean existsByUserAndToken(UserAccount user, String token);
    List<TokenFirebase> findByUser_Id(Long userId);
}
