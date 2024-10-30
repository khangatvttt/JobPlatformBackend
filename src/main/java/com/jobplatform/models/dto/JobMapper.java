package com.jobplatform.models.dto;

import com.jobplatform.models.Job;

public class JobMapper {

    public static JobDetailDto toJobDetailDto(Job job) {
        return new JobDetailDto(
                job.getTitle(),
                job.getDescription(),
                job.getWorkExperience(),
                job.getBenefits(),
                job.getUser().getCompany(),
                job.getSalary(),
                job.getDeadline(),
                job.getCreateAt()
        );
    }
}
