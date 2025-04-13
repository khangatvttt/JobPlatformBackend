package com.jobplatform.controllers;

import com.jobplatform.models.ChatMessage;
import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.Message;
import com.jobplatform.repositories.ChatMessageRepository;
import com.jobplatform.repositories.UserRepository;
import com.jobplatform.services.ChatMessageService;
import com.jobplatform.services.FirebaseService;
import com.jobplatform.services.NotificationService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.NoSuchElementException;

@Controller
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final FirebaseService firebaseService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ChatController(ChatMessageService chatMessageService, SimpMessagingTemplate messagingTemplate, FirebaseService firebaseService, UserRepository userRepository, NotificationService notificationService) {
        this.chatMessageService = chatMessageService;
        this.messagingTemplate = messagingTemplate;
        this.firebaseService = firebaseService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @MessageMapping("/chat")
    public void sendMessage(@Payload Message message,  Principal principal) {
        String userName = principal.getName();
        message.setSender(userName);
        chatMessageService.createMessage(message);
        String messageNoti = "Có tin nhắn mới từ " + userName;
        UserAccount userReceiver = userRepository.findByEmail(message.getReceiver()).orElseThrow(() -> new NoSuchElementException("User not found for notification"));
        notificationService.addNotification(messageNoti, "" ,userReceiver);
        firebaseService.sendNotification(userReceiver.getId(), messageNoti);
        messagingTemplate.convertAndSendToUser(
                message.getReceiver(), "/queue/messages", message);
    }
}