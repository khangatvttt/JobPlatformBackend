package com.jobplatform.services;

import com.jobplatform.models.ChatMessage;
import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.Message;
import com.jobplatform.repositories.ChatMessageRepository;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.naming.NoPermissionException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public void createMessage(Message message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(message.getContent());
        chatMessage.setSender(message.getSender());
        chatMessage.setReceiver(message.getReceiver());
        chatMessage.setDeleted(false);
        chatMessage.setDeletedAt(null);
        chatMessageRepository.save(chatMessage);
    }

    @SneakyThrows
    public void deleteMessage(Long id) {
        ChatMessage chatMessage = chatMessageRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Message not found"));
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!userAccount.getEmail().equals(chatMessage.getSender())) {
            throw new NoPermissionException("You are not allow to delete other user message");
        }
        chatMessage.setDeleted(true);
        chatMessage.setDeletedAt(LocalDateTime.now());
        chatMessageRepository.save(chatMessage);
    }

    @SneakyThrows
    public Page<ChatMessage> getChatHistory(String sender, String receiver, Pageable pageable) {
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!userAccount.getEmail().equals(sender)) {
            throw new NoPermissionException("You are not allow to delete other user message");
        }
        return chatMessageRepository.findChatHistory(sender, receiver, pageable);
    }

    @SneakyThrows
    public void editMessage(Long id, String content) {
        ChatMessage chatMessage = chatMessageRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Message not found"));
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!userAccount.getEmail().equals(chatMessage.getSender())) {
            throw new NoPermissionException("You are not allow to delete other user message");
        }
        chatMessage.setContent(content);
        chatMessageRepository.save(chatMessage);
    }
}
