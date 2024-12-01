package com.jobplatform.controllers;

import com.jobplatform.models.Review;
import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.ReviewDto;
import com.jobplatform.models.dto.ReviewMapper;
import com.jobplatform.models.dto.UserDto;
import com.jobplatform.services.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;

    public ReviewController(ReviewService reviewService, ReviewMapper reviewMapper) {
        this.reviewService = reviewService;
        this.reviewMapper = reviewMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getReview(@PathVariable Long id){
        return new ResponseEntity<>(reviewService.getReview(id), HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<List<ReviewDto>> getAllReview(@RequestParam(required = false) Long userId,
                                                         @RequestParam(required = false) Long jobId,
                                                         @RequestParam(required = false) Integer score,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size){
        Page<Review> users = reviewService.getAllReview(jobId, userId, score, page, size);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Pages", String.valueOf(users.getTotalPages()));
        headers.add("X-Total-Elements", String.valueOf(users.getTotalElements()));

        List<ReviewDto> reviewDtos = users.getContent().stream().map(reviewMapper::toDto).toList();
        return new ResponseEntity<>(reviewDtos, headers, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<ReviewDto> addReview(@RequestBody @Valid ReviewDto reviewDto){
        return new ResponseEntity<>(reviewService.addReview(reviewDto), HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable Long id,
                                                  @RequestBody @Valid ReviewDto reviewDto){
        return new ResponseEntity<>(reviewService.updateReview(id, reviewDto), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id){
        reviewService.deleteReview(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
