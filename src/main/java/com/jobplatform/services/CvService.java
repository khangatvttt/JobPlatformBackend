package com.jobplatform.services;

import com.jobplatform.models.Cv;
import com.jobplatform.models.Job;
import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.CvDto;
import com.jobplatform.models.dto.CvMapper;
import com.jobplatform.models.dto.JobMapper;
import com.jobplatform.models.dto.JobScore;
import com.jobplatform.repositories.CvRepository;
import com.jobplatform.repositories.JobRepository;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.naming.NoPermissionException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CvService {
    private final CvRepository cvRepository;
    private final CvMapper cvMapper;
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final JobService jobService;

    public CvService(CvRepository cvRepository, CvMapper cvMapper, JobRepository jobRepository, JobMapper jobMapper, JobService jobService) {
        this.cvRepository = cvRepository;
        this.cvMapper = cvMapper;
        this.jobRepository = jobRepository;
        this.jobMapper = jobMapper;
        this.jobService = jobService;
    }

    public Cv createCv(Cv cv) {
        // Get current logging user
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        cv.setId(null);
        cv.setUser(userAccount);
        cv.setCreatedAt(LocalDateTime.now());
        cv.setStatus(false);
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

    public void turnOnCv(Long cvId) {
        Cv updateCv = cvRepository.findById(cvId).orElseThrow(() -> new NoSuchElementException("CV with id " + cvId + " not found"));
        checkOwnership(updateCv.getUser().getId());
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<Cv> cvList = cvRepository.findByUserId(userAccount.getId());
        for (Cv eachCv : cvList) {
            eachCv.setStatus(Objects.equals(eachCv.getId(), cvId));
        }
        cvRepository.saveAll(cvList);
    }

    public void turnOffCv(Long cvId) {
        Cv updateCv = cvRepository.findById(cvId).orElseThrow(() -> new NoSuchElementException("CV with id " + cvId + " not found"));
        checkOwnership(updateCv.getUser().getId());
        updateCv.setStatus(false);
        cvRepository.save(updateCv);
    }

    public void deleteCv(Long cvId){
        Cv existingCv = cvRepository.findById(cvId).orElseThrow(() -> new NoSuchElementException("CV with id " + cvId + " not found"));
        checkOwnership(existingCv.getUser().getId());
        cvRepository.delete(existingCv);
    }

    @SneakyThrows
    public List<JobScore> findMatchJob(Long cvId, int limit) {
        Cv cv = cvRepository.findById(cvId).orElseThrow(() -> new NoSuchElementException("CV not found"));
        checkOwnership(cv.getUser().getId());
        List<Job> allJobs = jobRepository.findByStatus(Job.Status.SHOW);
        if (!cv.getUser().getIsPremium()) {
            throw new NoPermissionException();
        }
        return allJobs.stream()
                .map(job -> {
                    double score = jobService.matchCvToJob(job, cv);
                    return new JobScore(jobMapper.toDto(job), score);
                })
                .sorted(Comparator.comparingDouble(JobScore::score).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }


    @SneakyThrows
    private void checkOwnership(Long resourceOwnerId){
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userAccount.getRole()!= UserAccount.Role.ROLE_ADMIN && !userAccount.getId().equals(resourceOwnerId)){
            throw new NoPermissionException();
        }
    }
}
