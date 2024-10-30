package com.jobplatform.services;

import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.LoginDto;
import com.jobplatform.models.dto.LoginTokenDto;
import com.jobplatform.repositories.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.mail.MessagingException;
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

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final String baseURL_Frontend = "http://localhost:3000";

    public AccountService(UserRepository userRepository,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          JavaMailSender mailSender,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @SneakyThrows
    public void signUp(UserAccount userAccount, String baseUrl){
        Optional<UserAccount> userOpt = userRepository.findByEmail(userAccount.getEmail());
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
        userAccount.setIsNonLocked(true);
        userAccount.setIsActive(false);
        userAccount.setPassword(passwordEncoder.encode(userAccount.getPassword()));
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
        Optional<UserAccount> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()){
            return;
        }

        UserAccount user = userOpt.get();

        String senderName = "Job Platform Website";
        String from = "JobPlatformWebsite@gmail.com";
        String subject = "Reset mật khẩu cho Job Platform Website";
        String content = "Xin chào [[name]],<br>"
                + "Ai đó đã yêu cầu reset mật khẩu cho tài khoản Job Platform Website. Nếu đó là bạn, hãy nhấp vào link sau để xác nhận tài khoản của bạn:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">XÁC NHẬN</a></h3>"
                + "Nếu đó không phải là bạn, hãy bỏ qua mail này.<br>"
                + "Xin cảm ơn!";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(from,senderName);
        helper.setTo(user.getEmail());
        helper.setSubject(subject);

        content = content.replace("[[name]]", user.getFullName());
        //String code = UUID.randomUUID() + System.currentTimeMillis() +"";
        String verifyURL = baseURL_Frontend + "/auth/password-reset?token=";
        content = content.replace("[[URL]]", verifyURL);

        helper.setText(content, true);

        mailSender.send(message);
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
