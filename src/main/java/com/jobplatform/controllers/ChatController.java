package com.jobplatform.controllers;

import com.jobplatform.models.ChatMessage;
import com.jobplatform.models.dto.Message;
import com.jobplatform.repositories.ChatMessageRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatMessageRepository chatMessageRepository, SimpMessagingTemplate messagingTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat")
    public void sendMessage(@Payload Message message,  Principal principal) {
        String userName = principal.getName();
        message.setSender(userName);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(message.getContent());
        chatMessage.setSender(message.getSender());
        chatMessageRepository.save(chatMessage);
        messagingTemplate.convertAndSendToUser(
                message.getReceiver(), "/queue/messages", message);
    }
}