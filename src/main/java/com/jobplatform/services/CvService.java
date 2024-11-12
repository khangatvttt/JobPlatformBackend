package com.jobplatform.services;

import com.jobplatform.models.Cv;
import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.CvDto;
import com.jobplatform.models.dto.CvMapper;
import com.jobplatform.repositories.CvRepository;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.naming.NoPermissionException;
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

        cv.setId(null);
        cv.setUser(userAccount);
        cv.setCreatedAt(LocalDateTime.now());
        return cvRepository.save(cv);
    }

    public Cv getCv(Long cvId){
        return cvRepository.findById(cvId).orElseThrow(()-> new NoSuchElementException("Cv with id "+cvId+" not found"));
    }

    public Page<Cv> getCvs(Long userId, Integer page, Integer size) {
        Page<Cv> listCv;
        Pageable pageable = PageRequest.of(page, size);
        if (userId != null) {
            listCv = cvRepository.findByUserId(userId, pageable);
        } else {
            listCv = cvRepository.findAll(pageable);
        }
        return listCv;
    }

    public Cv updateCv(Long cvId, CvDto cvDto) {
        Cv existingCv = cvRepository.findById(cvId).orElseThrow(() -> new NoSuchElementException("CV with id " + cvId + " not found"));
        checkOwnership(existingCv.getUser().getId());
        cvMapper.updateCv(cvDto, existingCv);
        return cvRepository.save(existingCv);
    }

    public void deleteCv(Long cvId){
        Cv existingCv = cvRepository.findById(cvId).orElseThrow(() -> new NoSuchElementException("CV with id " + cvId + " not found"));
        checkOwnership(existingCv.getUser().getId());
        cvRepository.delete(existingCv);
    }

    @SneakyThrows
    private void checkOwnership(Long resourceOwnerId){
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userAccount.getRole()!= UserAccount.Role.ROLE_ADMIN && !userAccount.getId().equals(resourceOwnerId)){
            throw new NoPermissionException();
        }
    }
}
