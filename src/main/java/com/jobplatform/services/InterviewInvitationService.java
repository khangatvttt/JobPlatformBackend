package com.jobplatform.services;

import com.jobplatform.models.InterviewInvitation;
import com.jobplatform.models.Application;
import com.jobplatform.models.Job;
import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.InterviewInvitationDto;
import com.jobplatform.models.dto.InterviewInvitationExtendedDto;
import com.jobplatform.models.dto.InterviewInvitationMapper;
import com.jobplatform.repositories.InterviewInvitationRepository;
import com.jobplatform.repositories.ApplicationRepository;
import com.jobplatform.repositories.JobRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.SneakyThrows;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.persistence.EntityNotFoundException;

import javax.naming.NoPermissionException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class InterviewInvitationService {

    private final InterviewInvitationRepository interviewInvitationRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewInvitationMapper interviewInvitationMapper;
    private final JobRepository jobRepository;
    private final NotificationService notificationService;

    public InterviewInvitationService(InterviewInvitationRepository interviewInvitationRepository,
                                      ApplicationRepository applicationRepository,
                                      InterviewInvitationMapper interviewInvitationMapper, JobRepository jobRepository, NotificationService notificationService) {
        this.interviewInvitationRepository = interviewInvitationRepository;
        this.applicationRepository = applicationRepository;
        this.interviewInvitationMapper = interviewInvitationMapper;
        this.jobRepository = jobRepository;
        this.notificationService = notificationService;
    }

    @SneakyThrows
    public List<InterviewInvitationExtendedDto> getInterviewInvitations(Long jobId, Long userId) {
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (jobId==null && userId == null){
            if (userAccount.getRole()!= UserAccount.Role.ROLE_ADMIN){
                throw new NoPermissionException();
            }
        }
        else if (userId!=null) {
            if (!Objects.equals(userId, userAccount.getId()) && userAccount.getRole() != UserAccount.Role.ROLE_ADMIN) {
                throw new NoPermissionException();
            }
        }
        else {
            List<Long> jobIds = jobRepository.findByUserId(userAccount.getId()).stream()
                    .map(Job::getId)
                    .toList();
            if (!jobIds.contains(jobId) && userAccount.getRole() != UserAccount.Role.ROLE_ADMIN) {
                throw new NoPermissionException();
            }
        }
        List<InterviewInvitation> interviewInvitationList = interviewInvitationRepository
                .findAll(interviewFilter(userId, jobId));
        return interviewInvitationList.stream()
                .map(interviewInvitationMapper::toExtendedDto)
                .toList();
    }

    @SneakyThrows
    public InterviewInvitationExtendedDto getInterviewInvitation(Long id) {
        InterviewInvitation interviewInvitation = interviewInvitationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("InterviewInvitation not found"));
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!Objects.equals(interviewInvitation.getApplication().getUser().getId(), userAccount.getId())&&userAccount.getRole()!= UserAccount.Role.ROLE_ADMIN){
            throw new NoPermissionException();
        }
        return interviewInvitationMapper.toExtendedDto(interviewInvitation);
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

        //Notify to jobSeeker
        String message = "Bạn có thư mời phỏng vấn từ công việc '"+application.getJob().getTitle()+"'";
        String link = "";
        notificationService.addNotification(message, link ,application.getUser());

        //Update status for application
        application.setStatus(Application.Status.INTERVIEWING);
        applicationRepository.save(application);

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

    public static Specification<InterviewInvitation> interviewFilter(Long userId, Long jobId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                Join<InterviewInvitation, Application> applicationJoin = root.join("application");
                Join<Application, UserAccount> userJoin = applicationJoin.join("user");
                predicates.add(criteriaBuilder.equal(userJoin.get("id"), userId));
            }

            if (jobId != null) {
                Join<InterviewInvitation, Application> applicationJoin = root.join("application");
                Join<Application, Job> jobJoin = applicationJoin.join("job");
                predicates.add(criteriaBuilder.equal(jobJoin.get("id"), jobId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }


}
