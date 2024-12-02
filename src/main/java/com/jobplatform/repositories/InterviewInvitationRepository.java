package com.jobplatform.repositories;

import com.jobplatform.models.InterviewInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewInvitationRepository extends JpaRepository<InterviewInvitation, Long> {
}
