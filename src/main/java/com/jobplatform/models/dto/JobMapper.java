package com.jobplatform.models.dto;

import com.jobplatform.models.Cv;
import com.jobplatform.models.Job;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface JobMapper {


    Job toEntity(JobDetailDto jobDetailDto);

    @Mappings({
            @Mapping(source = "user.company.name", target = "companyName")
    })
    JobDetailDto toDto(Job job);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateJob(JobDetailDto sourceJob, @MappingTarget Job targetJob);

}
