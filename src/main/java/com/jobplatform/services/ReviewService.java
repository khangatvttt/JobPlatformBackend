package com.jobplatform.services;

import com.jobplatform.models.Job;
import com.jobplatform.models.Review;
import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.ReviewDto;
import com.jobplatform.models.dto.ReviewMapper;
import com.jobplatform.repositories.JobRepository;
import com.jobplatform.repositories.ReviewRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.SneakyThrows;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.naming.NoPermissionException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final JobRepository jobRepository;
    private final ReviewMapper reviewMapper;

    public ReviewService(ReviewRepository reviewRepository, JobRepository jobRepository, ReviewMapper reviewMapper) {
        this.reviewRepository = reviewRepository;
        this.jobRepository = jobRepository;
        this.reviewMapper = reviewMapper;
    }

    public ReviewDto getReview(Long id){
        return reviewMapper.toDto(reviewRepository
                .findById(id)
                .orElseThrow(()-> new NoSuchElementException("Review with id "+id+ "is not found")));
    }

    public Page<Review> getAllReview(Long jobId, Long userId, Integer score, int page, int size){
        Pageable pageable = PageRequest.of(page,size);
        return reviewRepository.findAll(reviewFilter(score, jobId, userId), pageable);
    }

    public ReviewDto addReview(ReviewDto reviewDto){
        Job job = jobRepository.findById(reviewDto.jobId()).orElseThrow(()-> new NoSuchElementException("Job with id "+reviewDto.jobId()+ "is not found"));
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Review review = reviewMapper.toEntity(reviewDto);

        if (reviewRepository.existsByUserIdAndJobId(userAccount.getId(), reviewDto.jobId())){
            throw new DuplicateKeyException("Review with jobId: "+ reviewDto.jobId()+" and userId: "+userAccount.getId()+" already existed");
        }

        review.setCreatedAt(LocalDateTime.now());
        review.setJob(job);
        review.setUser(userAccount);
        review.setId(null);

        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toDto(savedReview);
    }

    public ReviewDto updateReview(Long id, ReviewDto reviewDto){
        Review review = reviewRepository.findById(id).orElseThrow(()-> new NoSuchElementException("Review with id "+id+ "is not found"));
        checkOwnership(review.getUser().getId());
        reviewMapper.updateReview(reviewDto, review);
        return reviewMapper.toDto(reviewRepository.save(review));
    }

    public void deleteReview(Long id){
        Review review = reviewRepository.findById(id).orElseThrow(()-> new NoSuchElementException("Review with id "+id+ "is not found"));
        checkOwnership(review.getUser().getId());
        reviewRepository.delete(review);
    }


    public Specification<Review> reviewFilter(Integer score, Long jobId, Long userId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (score != null) {
                predicates.add(criteriaBuilder.equal(root.get("rating"), score));
            }

            if (userId != null) {
                Join<Review, UserAccount> userJoin = root.join("user");
                predicates.add(criteriaBuilder.equal(userJoin.get("id"), userId));
            }

            if (jobId != null) {
                Join<Review, Job> jobJoin = root.join("job");
                predicates.add(criteriaBuilder.equal(jobJoin.get("id"), jobId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @SneakyThrows
    private void checkOwnership(Long resourceOwnerId){
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userAccount.getRole()!= UserAccount.Role.ROLE_ADMIN && !userAccount.getId().equals(resourceOwnerId)){
            throw new NoPermissionException();
        }
    }

}
