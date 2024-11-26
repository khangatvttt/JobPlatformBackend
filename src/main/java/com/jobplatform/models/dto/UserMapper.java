package com.jobplatform.models.dto;

import com.jobplatform.models.UserAccount;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserAccount toEntity(UserDto userDto);

    @Mapping(target = "password", ignore = true)
    @Mappings({
            @Mapping(source = "company.name", target = "companyName"),
    })
    UserDto toDto(UserAccount user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isNonLocked", ignore = true)
    void updateUser(UserDto sourceUser, @MappingTarget UserAccount targetUser);

}
