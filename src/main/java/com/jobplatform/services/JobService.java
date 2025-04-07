package com.jobplatform.services;

import com.jobplatform.models.Cv;
import com.jobplatform.models.Job;
import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.*;
import com.jobplatform.repositories.CvRepository;
import com.jobplatform.repositories.JobRepository;
import com.jobplatform.repositories.UserRepository;
import com.jobplatform.utils.NormalizerUtils;
import com.jobplatform.utils.TextSimilarityUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.NoPermissionException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class JobService {
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobMapper jobMapper;
    private final NotificationService notificationService;
    private final FirebaseService firebaseService;
    private final CvRepository cvRepository;

    public JobService(JobRepository jobRepository, UserRepository userRepository, JobMapper jobMapper, NotificationService notificationService, FirebaseService firebaseService, CvRepository cvRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.jobMapper = jobMapper;
        this.notificationService = notificationService;
        this.firebaseService = firebaseService;
        this.cvRepository = cvRepository;
    }

    // Create a new job
    @Transactional
    @SneakyThrows
    public JobDetailDto addJob(JobDetailDto jobDetailDto) {
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userAccount.getAvailableJobPosts()==null || userAccount.getAvailableJobPosts()<=0){
            throw new BadRequestException("This account hasn't had enough number of job post allowed to post");
        }


        if (userAccount.getCompany() != null) {
            Hibernate.initialize(userAccount.getCompany());
        }


        Job job = jobMapper.toEntity(jobDetailDto);

        job.setCreateAt(LocalDateTime.now());
        job.setExpiredTime(LocalDateTime.now().plusDays(30));
        job.setUser(userAccount);
        job.setStatus(Job.Status.PENDING_APPROVAL);

        Job savedJob = jobRepository.save(job);

        //Reduce number of jobs user can post
        Integer jobPostsLeft = userAccount.getAvailableJobPosts()-1;
        userRepository.updateAvailableJobPosts(jobPostsLeft, userAccount.getId());
        userAccount.setAvailableJobPosts(jobPostsLeft);

        return jobMapper.toDto(savedJob);
    }

    // Get all jobs
    public Page<Job> findAllJobs(Pageable pageable, String title, Boolean related, String status, Long userId, String industry, String address) {
        // Check the status
        if (status!=null) {
            Job.Status.valueOf(status);
        }
        // Check the title
        if (title != null) {
            title = title.replace('-', ' ');
        }
        return jobRepository.findAll(jobFilter(title, related, status, userId, industry, address), pageable);
    }

    // Get a job by Id
    public JobDetailDto findJobById(Long id) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Job not found with id:" + id));
        return jobMapper.toDto(job);
    }

    // Update Job (partial update)
    @SneakyThrows
    public JobDetailDto updateJob(Long id, UpdateJobDto jobDetailDto) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Job with id: " + id +" is not found"));
        checkOwnership(job.getUser().getId());

        Job.Status oldStatus = job.getStatus();

        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (userAccount.getRole().equals(UserAccount.Role.ROLE_RECRUITER) && userAccount.getAvailableJobPosts() <= 0) {
            throw new BadRequestException("Out of credit job post");
        }
        jobMapper.updateJob(jobDetailDto, job);

        if (oldStatus == Job.Status.PENDING_APPROVAL && userAccount.getRole()!= UserAccount.Role.ROLE_ADMIN){
            job.setStatus(oldStatus);
        }
        if (userAccount.getRole()== UserAccount.Role.ROLE_ADMIN
                && (jobDetailDto.status()== Job.Status.DISQUALIFIED || jobDetailDto.status()== Job.Status.SHOW)
                    && oldStatus== Job.Status.PENDING_APPROVAL
        ){
            //Notify to recruiter
            String message = "Công việc '"+job.getTitle()+"' mà bạn đăng đã"+ (jobDetailDto.status()== Job.Status.DISQUALIFIED?" bị từ chối":" được phê duyệt");
            String link = "/job/"+job.getId();
            notificationService.addNotification(message, link ,job.getUser());
            firebaseService.sendNotification(job.getUser().getId(), message);
        }
        if (!userAccount.getRole().equals(UserAccount.Role.ROLE_ADMIN)) {
            job.setExpiredTime(job.getExpiredTime().plusDays(30));
            userAccount.setAvailableJobPosts(userAccount.getAvailableJobPosts() - 1);
            userRepository.save(userAccount);
        }
        Job updatedJob = jobRepository.save(job);
        return jobMapper.toDto(updatedJob);
    }

    // Delete job by Id
    public void deleteJob(Long id) {
        Job job = jobRepository.findById(id).orElseThrow(()->new NoSuchElementException("Job with id: " + id +" is not found"));
        checkOwnership(job.getUser().getId());
        jobRepository.deleteById(id);
    }

    // Search job function
    public static Specification<Job> jobFilter(String title, Boolean related, String status, Long userId, String industry, String address) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Title filter
            if (title != null && !title.isEmpty()) {
                if (related) {
                    String[] keywords = title.split(" ");
                    List<Predicate> keywordPredicates = new ArrayList<>();
                    for (String keyword : keywords) {
                        keywordPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"));
                    }
                    predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new Predicate[0])));
                } else {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
                }
            }

            // Status filter
            if (status != null && !status.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (userId != null) {
                Join<Job, UserAccount> userJoin = root.join("user");
                predicates.add(criteriaBuilder.equal(userJoin.get("id"), userId));
            }

            if (industry != null) {
                predicates.add(criteriaBuilder.equal(root.get("industry"), industry));
            }

            if (address != null) {
                predicates.add(criteriaBuilder.equal(root.get("address"), address));
            }

            predicates.add(criteriaBuilder.greaterThan(root.get("expiredTime"), LocalDateTime.now()));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public double matchCvToJob(Job job, Cv cv) {
        double positionMatchScore = TextSimilarityUtils.cosineSimilarity(cv.getJobPosition(), job.getTitle());
        double experienceMatchScore = TextSimilarityUtils.cosineSimilarity(cv.getWorkExperience(), job.getDescription());
        double skillsMatchScore = TextSimilarityUtils.jaccardSimilarity(cv.getSkills(), job.getRequirement());
        double languageMatchScore = TextSimilarityUtils.cosineSimilarity(cv.getLanguageSkill(), job.getRequirement());
        double levelMatchScore = NormalizerUtils.normalizeLevel(cv.getJobPosition(), job.getLevel()) ? 1.0 : 0.0;
        double industryMatchScore = NormalizerUtils.normalizeIndustry(cv.getJobPosition(), job.getIndustry()) ? 1.0 : 0.0;

        // Weighted scoring
        double match =
                positionMatchScore * 0.2 +
                experienceMatchScore * 0.1 +
                skillsMatchScore * 0.25 +
                languageMatchScore * 0.2 +
                levelMatchScore * 0.1 +
                industryMatchScore * 0.15;

        return Math.round(match * 10000.0) / 100.0;
    }

    @SneakyThrows
    public List<CvScore> findBestCvMatchesForJob(Long jobId, int limit) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NoSuchElementException("Job not found"));
        checkOwnership(job.getUser().getId());
        if (!job.getUser().getIsPremium()) {
            throw new NoPermissionException("Only premium account");
        }
        List<Cv> allCvs = cvRepository.findByStatus(true);

        return allCvs.stream()
                .map(cv -> new CvScore(cv, matchCvToJob(job, cv)))
                .sorted(Comparator.comparingDouble(CvScore::score).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public void extendExpireTimeJob(Long jobId) {
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userAccount.getAvailableJobPosts() <= 0) {
            throw new BadRequestException("Out of credit for job posts");
        }
        Job job = jobRepository.findById(jobId).orElseThrow(()-> new NoSuchElementException("Job not found"));
        job.setExpiredTime(job.getExpiredTime().plusDays(30));
        jobRepository.save(job);
        userAccount.setAvailableJobPosts(userAccount.getAvailableJobPosts() - 1);
        userRepository.save(userAccount);
    }


    @SneakyThrows
    private void checkOwnership(Long resourceOwnerId){
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userAccount.getRole()!= UserAccount.Role.ROLE_ADMIN && !userAccount.getId().equals(resourceOwnerId)){
            throw new NoPermissionException();
        }
    }

}
