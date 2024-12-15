package com.jobplatform.repositories;

import com.jobplatform.models.InterviewInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InterviewInvitationRepository extends JpaRepository<InterviewInvitation, Long>, JpaSpecificationExecutor<InterviewInvitation> {
}
