package com.jobplatform.models.dto;

import com.jobplatform.models.Job;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface JobMapper {


    Job toEntity(JobDetailDto jobDetailDto);

    @Mappings({
            @Mapping(source = "user.company.name", target = "companyName"),
            @Mapping(source = "user.company.images", target = "companyImages"),
            @Mapping(source = "user.company.location", target = "companyLocation"),
            @Mapping(source = "user.company.companySize", target = "companySize")

    })
    JobDetailDto toDto(Job job);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateJob(JobDetailDto sourceJob, @MappingTarget Job targetJob);

}
