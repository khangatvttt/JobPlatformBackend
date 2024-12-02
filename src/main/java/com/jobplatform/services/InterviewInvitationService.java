package com.jobplatform.services;

import com.jobplatform.models.InterviewInvitation;
import com.jobplatform.models.Application;
import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.InterviewInvitationDto;
import com.jobplatform.models.dto.InterviewInvitationMapper;
import com.jobplatform.repositories.InterviewInvitationRepository;
import com.jobplatform.repositories.ApplicationRepository;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.persistence.EntityNotFoundException;

import javax.naming.NoPermissionException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class InterviewInvitationService {

    private final InterviewInvitationRepository interviewInvitationRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewInvitationMapper interviewInvitationMapper;

    public InterviewInvitationService(InterviewInvitationRepository interviewInvitationRepository,
                                      ApplicationRepository applicationRepository,
                                      InterviewInvitationMapper interviewInvitationMapper) {
        this.interviewInvitationRepository = interviewInvitationRepository;
        this.applicationRepository = applicationRepository;
        this.interviewInvitationMapper = interviewInvitationMapper;
    }

    @SneakyThrows
    public InterviewInvitationDto getInterviewInvitation(Long id) {
        InterviewInvitation interviewInvitation = interviewInvitationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("InterviewInvitation not found"));
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!Objects.equals(interviewInvitation.getApplication().getUser().getId(), userAccount.getId())&&userAccount.getRole()!= UserAccount.Role.ROLE_ADMIN){
            throw new NoPermissionException();
        }
        return interviewInvitationMapper.toDto(interviewInvitation);
    }

    @SneakyThrows
    public InterviewInvitationDto createInterviewInvitation(InterviewInvitationDto interviewInvitationDto) {
        Application application = applicationRepository.findById(interviewInvitationDto.applicationId())
                .orElseThrow(() -> new NoSuchElementException("Application with id " + interviewInvitationDto.applicationId() + " is not found"));

        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!Objects.equals(application.getJob().getUser().getId(), userAccount.getId())) {
            throw new NoPermissionException();
        }

        InterviewInvitation interviewInvitation = interviewInvitationMapper.toEntity(interviewInvitationDto);
        interviewInvitation.setCreateAt(LocalDateTime.now());
        interviewInvitation.setApplication(application);
        interviewInvitation.setId(null);

        InterviewInvitation savedInterviewInvitation = interviewInvitationRepository.save(interviewInvitation);
        return interviewInvitationMapper.toDto(savedInterviewInvitation);
    }

    @SneakyThrows
    public InterviewInvitationDto updateInterviewInvitation(Long id, InterviewInvitationDto interviewInvitationDto) {
        InterviewInvitation existingInvitation = interviewInvitationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("InterviewInvitation not found"));

        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!Objects.equals(existingInvitation.getApplication().getJob().getUser().getId(), userAccount.getId())) {
            throw new NoPermissionException();
        }

        interviewInvitationMapper.updateInterviewInvitation(interviewInvitationDto, existingInvitation);
        return interviewInvitationMapper.toDto(interviewInvitationRepository.save(existingInvitation));
    }


}
