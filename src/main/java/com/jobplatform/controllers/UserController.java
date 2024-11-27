package com.jobplatform.controllers;

import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.UserDto;
import com.jobplatform.models.dto.UserMapper;
import com.jobplatform.services.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
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
}

