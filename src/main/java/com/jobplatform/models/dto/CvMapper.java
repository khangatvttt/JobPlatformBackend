package com.jobplatform.models.dto;

import com.jobplatform.models.Cv;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CvMapper {

    Cv toEntity(CvDto cvDTO);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCv(CvDto sourceCv, @MappingTarget Cv targetCv);
}
