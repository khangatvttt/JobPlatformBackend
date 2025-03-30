package com.jobplatform.models.dto;

import com.jobplatform.models.Question;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface QuestionMapper {
    void updateQuestion(Question source, @MappingTarget Question target);
}
