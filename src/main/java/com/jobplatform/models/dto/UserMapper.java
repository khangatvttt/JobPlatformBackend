package com.jobplatform.models.dto;

import com.jobplatform.models.UserAccount;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserAccount toEntity(UserDto userDto);

    @Mapping(target = "password", ignore = true)
    UserDto toDto(UserAccount user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "email", ignore = true)  // Ignore email field
    @Mapping(target = "role", ignore = true)
    void updateUser(UserDto sourceUser, @MappingTarget UserAccount targetUser);

}
