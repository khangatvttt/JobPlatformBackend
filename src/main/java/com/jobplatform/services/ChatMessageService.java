package com.jobplatform.services;

import com.jobplatform.models.ChatMessage;
import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.Message;
import com.jobplatform.models.dto.UserDto;
import com.jobplatform.models.dto.UserMapper;
import com.jobplatform.repositories.ChatMessageRepository;
import com.jobplatform.repositories.UserRepository;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.naming.NoPermissionException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.stream.Collectors.toList;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public ChatMessageService(ChatMessageRepository chatMessageRepository, UserRepository userRepository, UserMapper userMapper) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
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

    @SneakyThrows
    public List<UserDto> getAllReceiver() {
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<ChatMessage> listChatMessage = chatMessageRepository.findDistinctBySenderOrderByCreatedAt(userAccount.getEmail());
        List<String> listReceiverFind = listChatMessage.stream().map(ChatMessage::getReceiver).toList();
        List<String> listReceiver = listReceiverFind.stream()
                .distinct()
                .toList();
        List<UserDto> result = new ArrayList<>();
        for (String userEmail : listReceiver) {
            UserAccount user = userRepository.findByEmail(userEmail).get();
            UserDto userDto = userMapper.toDto(user);
            result.add(userDto);
        }
        return result;
    }
}
