package com.jobplatform.controllers;

import com.jobplatform.models.Notification;
import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.UserDto;
import com.jobplatform.models.dto.UserMapper;
import com.jobplatform.services.NotificationService;
import com.jobplatform.services.TokenFirebaseService;
import com.jobplatform.services.UserService;

import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final TokenFirebaseService tokenFirebaseService;

    public UserController(UserService userService, UserMapper userMapper, NotificationService notificationService, TokenFirebaseService tokenFirebaseService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
        this.tokenFirebaseService = tokenFirebaseService;
    }

    @GetMapping("")
    public ResponseEntity<List<UserDto>> findAllUsers(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(required = false) String role,
                                                      @RequestParam(required = false) String email) {
        Page<UserAccount> users = userService.findAllUsers(page, size, role, email);
        List<UserDto> userDtos = users.getContent().stream().map(userMapper::toDto).toList();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Pages", String.valueOf(users.getTotalPages()));
        headers.add("X-Total-Elements", String.valueOf(users.getTotalElements()));

        return new ResponseEntity<>(userDtos, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findUserById(@PathVariable Long id) {
        UserDto userDto = userService.findUserById(id);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(id, userDto);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @GetMapping("/{id}/notifications")
    public ResponseEntity<List<Notification>> updateUser(@PathVariable Long id,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size
                                                   ) {
        Page<Notification> notificationPage = notificationService.getNotifications(id, page, size);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Pages", String.valueOf(notificationPage.getTotalPages()));
        headers.add("X-Total-Elements", String.valueOf(notificationPage.getTotalElements()));
        return new ResponseEntity<>(notificationPage.getContent(),headers, HttpStatus.OK);
    }

    @SneakyThrows
    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword (@PathVariable Long id,
                                                @RequestBody Map<String, String> payload){
        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");
        userService.changePassword(id, oldPassword, newPassword);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/fcm-token")
    public ResponseEntity<UserDto> addFCMToken(@RequestBody Map<String, String> payload) {
        String token = payload.get("FCMToken");
        tokenFirebaseService.addToken(token);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

