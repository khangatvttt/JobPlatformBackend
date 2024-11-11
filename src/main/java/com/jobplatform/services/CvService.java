package com.jobplatform.services;

import com.jobplatform.models.Cv;
import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.CvDto;
import com.jobplatform.models.dto.CvMapper;
import com.jobplatform.repositories.CvRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CvService {
    private final CvRepository cvRepository;
    private final CvMapper cvMapper;

    public CvService(CvRepository cvRepository, CvMapper cvMapper) {
        this.cvRepository = cvRepository;
        this.cvMapper = cvMapper;
    }

    public Cv createCv(Cv cv) {
        // Get current logging user
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        cv.setUser(userAccount);
        cv.setCreatedAt(LocalDateTime.now());
        return cvRepository.save(cv);
    }

    public List<Cv> getCvs(String userId) {
        List<Cv> listCv;
        if (userId != null) {
            listCv = cvRepository.findByUserId(Long.valueOf(userId));
        } else {
            listCv = cvRepository.findAll();
        }
        return listCv;
    }

    public Cv updateCv(Long cvId, CvDto cvDto) {
        Cv existingCv = cvRepository.findById(cvId).orElseThrow(() -> new NoSuchElementException("CV with id " + cvId + " not found"));
        cvMapper.updateCvFromEntity(cvDto, existingCv);
        return cvRepository.save(existingCv);
    }

    public void deleteCv(Long cvId){
        Cv existingCv = cvRepository.findById(cvId).orElseThrow(() -> new NoSuchElementException("CV with id " + cvId + " not found"));
        cvRepository.delete(existingCv);
    }

}
