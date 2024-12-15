package com.jobplatform.controllers;

import com.jobplatform.models.dto.InterviewInvitationDto;
import com.jobplatform.models.dto.InterviewInvitationExtendedDto;
import com.jobplatform.services.InterviewInvitationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/interview-invitations")
public class InterviewInvitationController {

    private final InterviewInvitationService interviewInvitationService;

    public InterviewInvitationController(InterviewInvitationService interviewInvitationService) {
        this.interviewInvitationService = interviewInvitationService;
    }

    @GetMapping("")
    public ResponseEntity<List<InterviewInvitationExtendedDto>> getInterviewInvitations(@RequestParam(required = false) Long jobId,
                                                                                        @RequestParam(required = false) Long userId) {
        return new ResponseEntity<>(interviewInvitationService.getInterviewInvitations(jobId, userId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterviewInvitationExtendedDto> getInterviewInvitation(@PathVariable Long id) {
        return new ResponseEntity<>(interviewInvitationService.getInterviewInvitation(id), HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<InterviewInvitationDto> createInterviewInvitation(@RequestBody InterviewInvitationDto interviewInvitationDto) {
        return new ResponseEntity<>(interviewInvitationService.createInterviewInvitation(interviewInvitationDto), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<InterviewInvitationDto> updateInterviewInvitation(@PathVariable Long id, @RequestBody InterviewInvitationDto interviewInvitationDto) {
        return new ResponseEntity<>(interviewInvitationService.updateInterviewInvitation(id, interviewInvitationDto), HttpStatus.OK);
    }

}
