package com.jobplatform.services;

import com.jobplatform.models.CvFile;
import com.jobplatform.models.UserAccount;
import com.jobplatform.repositories.CvFileRepository;
import com.jobplatform.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CvFileService {

    private final CvFileRepository cvFileRepository;
    private final UserRepository userRepository;

    public CvFileService(CvFileRepository cvFileRepository, UserRepository userRepository) {
        this.cvFileRepository = cvFileRepository;
        this.userRepository = userRepository;
    }

    public List<CvFile> getAllCvFileByUser(Long userId){
        UserAccount userAccount = userRepository.findById(userId).orElseThrow(
                () -> new NoSuchElementException("User with id " + userId + "is not found"));
        return cvFileRepository.findByUser(userAccount);
    }

    public CvFile getCvFile(Long cvFileId) {
        return cvFileRepository.findById(cvFileId).orElseThrow(
                ()-> new NoSuchElementException("Cv file with id " + cvFileId + "is not found"));
    }

}
