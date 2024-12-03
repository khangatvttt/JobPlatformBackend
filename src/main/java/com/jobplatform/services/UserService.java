package com.jobplatform.services;

import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.UserDto;
import com.jobplatform.models.dto.UserMapper;
import com.jobplatform.repositories.UserRepository;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.NoPermissionException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    public Page<UserAccount> findAllUsers(int page, int size, String role, String email) {
        Pageable pageable = PageRequest.of(page, size);
        UserAccount.Role roleUser;
        if (role==null) {
            roleUser = null;
        }
        else {
            roleUser = UserAccount.Role.valueOf(role);
        }
        return userRepository.findAll(filterUsers(roleUser, email), pageable);
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
        UserAccount.Role currentRole = checkOwnership(id);
        UserAccount user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if(currentRole== UserAccount.Role.ROLE_ADMIN){
            userMapper.updateUserAdmin(userDto, user);
        }
        else {
            userMapper.updateUser(userDto, user);
        }

        if (userDto.password() != null) {
            user.setPassword(passwordEncoder.encode(userDto.password()));
        }

        UserAccount updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @SneakyThrows
    private UserAccount.Role checkOwnership(Long resourceOwnerId){
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userAccount.getRole()!= UserAccount.Role.ROLE_ADMIN && !userAccount.getId().equals(resourceOwnerId)){
            throw new NoPermissionException();
        }
        return userAccount.getRole();
    }

    @SneakyThrows
    public void changePassword(Long id, String oldPassword, String newPassword){
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{6,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(newPassword);
        if (!matcher.matches()) {
            throw new BadRequestException("Password is not strong enough. Must have at least 6 characters, one uppercase, one lowercase, and one digit");
        }

        UserAccount user = userRepository.findById(id).orElseThrow(()->new NoSuchElementException("User id not found"));
        UserAccount userAccount = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!Objects.equals(userAccount.getId(), user.getId())){
            throw new NoPermissionException();
        }
        boolean isPasswordMatch = passwordEncoder.matches(oldPassword, user.getPassword());
        if (!isPasswordMatch){
            throw new NoPermissionException("Old password is not match");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public static Specification<UserAccount> filterUsers(UserAccount.Role role, String email) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();

            if (role != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("role"), role));
            }
            if (email != null && !email.isEmpty()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }
            return predicate;
        };
    }
}
