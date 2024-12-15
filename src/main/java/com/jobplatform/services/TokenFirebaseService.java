package com.jobplatform.services;

import com.jobplatform.models.TokenFirebase;
import com.jobplatform.models.UserAccount;
import com.jobplatform.repositories.TokenFirebaseRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TokenFirebaseService {

    private final TokenFirebaseRepository tokenFirebaseRepository;

    public TokenFirebaseService(TokenFirebaseRepository tokenFirebaseRepository) {
        this.tokenFirebaseRepository = tokenFirebaseRepository;
    }

    public void addToken(String token){
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (tokenFirebaseRepository.existsByUserAndToken(userAccount, token)){
            return;
        }

        TokenFirebase tokenFirebase = new TokenFirebase();
        tokenFirebase.setToken(token);
        tokenFirebase.setUser(userAccount);
        tokenFirebase.setCreatedAt(LocalDateTime.now());

        tokenFirebaseRepository.save(tokenFirebase);
    }

    public List<String> getToken(Long userId){
        return tokenFirebaseRepository.findByUser_Id(userId)
                .stream()
                .map(TokenFirebase::getToken)
                .toList();
    }
}
