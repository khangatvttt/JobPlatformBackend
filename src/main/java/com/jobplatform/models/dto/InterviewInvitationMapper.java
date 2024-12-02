package com.jobplatform.models.dto;

import com.jobplatform.models.InterviewInvitation;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InterviewInvitationMapper {

    InterviewInvitation toEntity(InterviewInvitationDto interviewInvitationDto);

    @Mappings({
            @Mapping(source = "application.id", target = "applicationId"),
    })
    InterviewInvitationDto toDto(InterviewInvitation interviewInvitation);

    @Mappings({
            @Mapping(source = "application.user.id", target = "userId"),
            @Mapping(source = "application.job.id", target = "jobId")
    })
    InterviewInvitationExtendedDto toExtendedDto(InterviewInvitation interviewInvitation);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateInterviewInvitation(InterviewInvitationDto source, @MappingTarget InterviewInvitation target);
}
