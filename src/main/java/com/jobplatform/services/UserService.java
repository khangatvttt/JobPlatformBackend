package com.jobplatform.services;

import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.UserDto;
import com.jobplatform.models.dto.UserMapper;
import com.jobplatform.repositories.UserRepository;
import lombok.SneakyThrows;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.NoPermissionException;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }
    
    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto findUserById(Long id) {
        UserAccount user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User with id " + id + " is not found"));
        return userMapper.toDto(user);
    }

    public void deleteUser(Long id) {
        UserAccount user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User with id " + id + " is not found"));
        userRepository.delete(user);
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        checkOwnership(id);
        UserAccount user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        userMapper.updateUser(userDto, user);

        if (userDto.password() != null) {
            user.setPassword(passwordEncoder.encode(userDto.password()));
        }

        UserAccount updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @SneakyThrows
    private void checkOwnership(Long resourceOwnerId){
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userAccount.getRole()!= UserAccount.Role.ROLE_ADMIN && !userAccount.getId().equals(resourceOwnerId)){
            throw new NoPermissionException();
        }
    }
}
