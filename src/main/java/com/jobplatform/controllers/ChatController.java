package com.jobplatform.controllers;

import com.jobplatform.models.ChatMessage;
import com.jobplatform.models.dto.Message;
import com.jobplatform.repositories.ChatMessageRepository;
import com.jobplatform.services.ChatMessageService;
import com.jobplatform.services.FirebaseService;
import com.jobplatform.services.NotificationService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final FirebaseService firebaseService;

    public ChatController(ChatMessageService chatMessageService, SimpMessagingTemplate messagingTemplate, FirebaseService firebaseService) {
        this.chatMessageService = chatMessageService;
        this.messagingTemplate = messagingTemplate;
        this.firebaseService = firebaseService;
    }

    @MessageMapping("/chat")
    public void sendMessage(@Payload Message message,  Principal principal) {
        String userName = principal.getName();
        message.setSender(userName);
        chatMessageService.createMessage(message);
        firebaseService.sendNotification(message.getReceiver(), "Có tin nhắn mới từ " + userName);
        messagingTemplate.convertAndSendToUser(
                message.getReceiver(), "/queue/messages", message);
    }
}