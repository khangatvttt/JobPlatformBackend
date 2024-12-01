package com.jobplatform.models.dto;

import com.jobplatform.models.Review;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    
    Review toEntity(ReviewDto reviewDto);

    @Mappings({
            @Mapping(source = "user.id", target = "userId"),
            @Mapping(source = "job.id", target = "jobId"),
    })
    ReviewDto toDto(Review review);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateReview(ReviewDto sourceReview, @MappingTarget Review targetReview);
}
