package com.jobplatform.controllers;

import com.jobplatform.models.ChatMessage;
import com.jobplatform.models.dto.ChatMessageDto;
import com.jobplatform.models.dto.ChatMessageMapper;
import com.jobplatform.models.dto.UserDto;
import com.jobplatform.services.ChatMessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat-message")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final ChatMessageMapper chatMessageMapper;

    public ChatMessageController(ChatMessageService chatMessageService, ChatMessageMapper chatMessageMapper) {
        this.chatMessageService = chatMessageService;
        this.chatMessageMapper = chatMessageMapper;
    }

    @GetMapping("")
    public ResponseEntity<List<ChatMessageDto>> getChatMessage(@RequestParam String sender,
                                                               @RequestParam String receiver,
                                                               @RequestParam int page,
                                                               @RequestParam int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> chatMessagePage = chatMessageService.getChatHistory(sender, receiver, pageable);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Pages", String.valueOf(chatMessagePage.getTotalPages()));
        headers.add("X-Total-Elements", String.valueOf(chatMessagePage.getTotalElements()));
        List<ChatMessageDto> chatMessageDtoList = chatMessagePage.getContent().stream().map(chatMessageMapper::toDto).toList();
        return new ResponseEntity<>(chatMessageDtoList, headers, HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id){
        chatMessageService.deleteMessage(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> editMessage(@PathVariable Long id,
                                                @RequestBody Map<String, String> payload){
        chatMessageService.editMessage(id, payload.get("content"));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/receiver")
    public ResponseEntity<List<UserDto>> getAllReceiver() {
        return new ResponseEntity<>(chatMessageService.getAllReceiver(),HttpStatus.OK);
    }

}
