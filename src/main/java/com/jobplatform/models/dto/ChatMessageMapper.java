package com.jobplatform.models.dto;

import com.jobplatform.models.Application;
import com.jobplatform.models.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    @Mapping(target = "content", expression = "java(message.isDeleted() ? \"\" : message.getContent())")
    @Mapping(target = "isDeleted", source = "deleted")
    ChatMessageDto toDto(ChatMessage message);

}
