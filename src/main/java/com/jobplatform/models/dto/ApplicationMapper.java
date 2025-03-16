package com.jobplatform.models.dto;

import com.jobplatform.models.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {
    Application toEntity(ApplicationDto applicationDto);

    @Mappings({
            @Mapping(source = "job.id", target = "jobId"),
            @Mapping(source = "user.id", target = "userId"),
            @Mapping(source = "job.user.company.name", target = "companyName"),
            @Mapping(source = "job.user.fullName", target = "recruiterName"),
            @Mapping(source = "job.user.company.images", target = "companyImage"),
            @Mapping(source = "job.user.email", target = "recruiterEmail")
    })
    ApplicationDto toDto(Application application);

}
