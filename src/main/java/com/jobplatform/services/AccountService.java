package com.jobplatform.services;

import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.LoginDto;
import com.jobplatform.models.dto.LoginTokenDto;
import com.jobplatform.models.dto.UserDto;
import com.jobplatform.models.dto.UserMapper;
import com.jobplatform.repositories.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AccountService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final String baseURL_Frontend = "http://localhost:3000";

    public AccountService(UserRepository userRepository,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          JavaMailSender mailSender,
                          PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.userMapper = userMapper;
    }

    @SneakyThrows
    public void signUp(UserDto userDto, String baseUrl){
        Optional<UserAccount> userOpt = userRepository.findByEmail(userDto.email());
        if (userOpt.isPresent()) {
            // This email has been used to activate account
            if (userOpt.get().getIsActive()) {
                throw new DuplicateKeyException("This email have been used");
            }
            // Email has been used to sign up but not be verified
            else {
                userRepository.delete(userOpt.get());
            }
        }

        UserAccount userAccount = userMapper.toEntity(userDto);

        userAccount.setIsNonLocked(true);
        userAccount.setIsActive(false);
        userAccount.setPassword(passwordEncoder.encode(userAccount.getPassword()));

        if (userAccount.getRole() == UserAccount.Role.ROLE_ADMIN){
            userAccount.setIsNonLocked(false);
        }

        sendVerifyMail(userAccount, baseUrl);
        userRepository.save(userAccount);

    }

    public LoginTokenDto login(LoginDto loginDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password())
        );

        UserAccount authenticatedUser = userRepository.findByEmail(loginDto.email()).orElseThrow();

        String jwtAccessToken = jwtService.generateToken(JwtService.TokenType.ACCESS_TOKEN,authenticatedUser);
        String jwtRefreshToken = jwtService.generateRefreshToken(authenticatedUser);

        authenticatedUser.setRefreshToken(jwtRefreshToken);
        userRepository.save(authenticatedUser);

        return new LoginTokenDto(jwtAccessToken,jwtRefreshToken);
    }

    public LoginTokenDto refresh(String oldRefreshToken){
        String emailUser = jwtService.extractUsername(oldRefreshToken);
        UserAccount user = checkUserEmail(emailUser);
        String refreshToken = user.getRefreshToken();
        if (!Objects.equals(refreshToken, oldRefreshToken)){
            throw new JwtException("Invalid token");
        }

        String jwtAccessToken = jwtService.generateToken(JwtService.TokenType.ACCESS_TOKEN,user);
        String jwtRefreshToken = jwtService.generateRefreshToken(oldRefreshToken, user);

        user.setRefreshToken(jwtRefreshToken);
        userRepository.save(user);

        return new LoginTokenDto(jwtAccessToken,jwtRefreshToken);
    }

    @SneakyThrows
    public void verifyEmail(String token){
        String tokenType = jwtService.getTokenType(token);
        if (!Objects.equals(tokenType, JwtService.TokenType.VERIFICATION_TOKEN.toString())){
            throw new JwtException("Invalid token");
        }
        String email = jwtService.extractUsername(token);
        UserAccount user = checkUserEmail(email);
        // Already activated account
        if (user.getIsActive()){
            throw new BadRequestException("This email has been activated");
        }
        user.setIsActive(true);
        userRepository.save(user);
    }

    @SneakyThrows
    public void sendResetPasswordEmail(String email) {
        UserAccount user = checkUserEmail(email);

        String senderName = "Job Platform Website";
        String from = "JobPlatformWebsite@gmail.com";
        String subject = "Reset mật khẩu cho Job Platform Website";
        String content = "Xin chào [[name]],<br>"
                + "Ai đó đã yêu cầu reset mật khẩu cho tài khoản Job Platform Website. Nếu đó là bạn, hãy nhấp vào link sau để reset mật khẩu của bạn:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">RESET MẬT KHẨU</a></h3>"
                + "Nếu đó không phải là bạn, hãy bỏ qua mail này.<br>"
                + "Xin cảm ơn!";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(from,senderName);
        helper.setTo(user.getEmail());
        helper.setSubject(subject);


        String code = UUID.randomUUID().toString();
        user.setResetPasswordToken(code);
        //Set expiration 30m from now
        user.setResetPasswordTokenExpiration(LocalDateTime.now().plusMinutes(30));
        String resetURL = baseURL_Frontend + "/auth/password-reset?token=" + code;
        userRepository.save(user);

        content = content.replace("[[name]]", user.getFullName());
        content = content.replace("[[URL]]", resetURL);

        helper.setText(content, true);

        mailSender.send(message);
    }

    @SneakyThrows
    public void resetPassword(String token, String newPassword){
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$";
        Pattern pattern = Pattern.compile(passwordPattern);
        Matcher matcher = pattern.matcher(newPassword);
        if (!matcher.matches()){
            throw new BadRequestException("Password is not strong enough. Must have at least 6 characters and contains at least one uppercase, one lowercase and one number");
        }

        Optional<UserAccount> userOpt = userRepository.findByResetPasswordToken(token);
        if (userOpt.isEmpty()){
            throw new BadRequestException("Reset password token is invalid");
        }

        UserAccount user = userOpt.get();
        if (user.getResetPasswordTokenExpiration().isBefore(LocalDateTime.now())){
            throw new BadRequestException("Reset password token expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiration(null);
        userRepository.save(user);
    }

    @SneakyThrows
    public void sendVerifyMail(UserAccount user, String baseURL){
        String senderName = "Job Platform Website";
        String from = "JobPlatformWebsite@gmail.com";
        String subject = "Xác nhận email";
        String content = "Xin chào [[name]],<br>"
                + "Ai đó đã sử dụng email của bạn để đăng kí tài khoản trên Job Platform Website. Nếu đó là bạn, hãy nhấp vào link sau để xác nhận tài khoản của bạn:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">XÁC NHẬN</a></h3>"
                + "Nếu đó không phải là bạn, hãy bỏ qua mail này.<br>"
                + "Xin cảm ơn!";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(from,senderName);
        helper.setTo(user.getEmail());
        helper.setSubject(subject);
        baseURL = "http://"+ baseURL;

        content = content.replace("[[name]]", user.getFullName());
        String verifyURL = baseURL + "/auth/verify-email?token=" + jwtService.generateToken(JwtService.TokenType.VERIFICATION_TOKEN,user);
        content = content.replace("[[URL]]", verifyURL);

        helper.setText(content, true);

        mailSender.send(message);
    }

    private UserAccount checkUserEmail(String email){
        Optional<UserAccount> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()){
            throw new NoSuchElementException("User with email "+email+" doesn't exist");
        }
        return userOpt.get();
    }

}
