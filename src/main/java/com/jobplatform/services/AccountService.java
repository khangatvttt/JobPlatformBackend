package com.jobplatform.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jobplatform.models.Company;
import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.LoginDto;
import com.jobplatform.models.dto.LoginTokenDto;
import com.jobplatform.models.dto.UserDto;
import com.jobplatform.models.dto.UserMapper;
import com.jobplatform.repositories.CompanyRepository;
import com.jobplatform.repositories.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
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
    private final CompanyRepository companyRepository;
    private final String baseURL_Frontend = "http://localhost:3000";
    private final String defaultAvatar = "https://firebasestorage.googleapis.com/v0/b/hotel-management-db2db.appspot.com/o/avatar-default.jpg?alt=media&token=1785bd27-d3da-4625-a4f8-87c706f73ce2";
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    public AccountService(UserRepository userRepository,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          JavaMailSender mailSender,
                          PasswordEncoder passwordEncoder, UserMapper userMapper, CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.userMapper = userMapper;
        this.companyRepository = companyRepository;
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
        userAccount.setCreatedAt(LocalDateTime.now());
        userAccount.setAvatarUrl(defaultAvatar);
        userAccount.setPassword(passwordEncoder.encode(userAccount.getPassword()));



        if (userAccount.getRole() == UserAccount.Role.ROLE_ADMIN ||
                userAccount.getRole() == UserAccount.Role.ROLE_RECRUITER){
            userAccount.setIsNonLocked(false);
        }

        if (userAccount.getRole() == UserAccount.Role.ROLE_RECRUITER){
            Company company = companyRepository.findById(userDto.companyId())
                    .orElseThrow(()->new NoSuchElementException("Company with id "+ userDto.companyId()+ " is not found"));
            userAccount.setCompany(company);
            userAccount.setAvailableJobPosts(3);
        }

        sendVerifyMail(userAccount, baseUrl);
        userRepository.save(userAccount);

    }

    public LoginTokenDto processGrantCode(String code) {
        String accessToken = getOauthAccessTokenGoogle(code);

        return getProfileDetailsGoogle(accessToken);
    }

    private LoginTokenDto getProfileDetailsGoogle(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

        String url = "https://www.googleapis.com/oauth2/v2/userinfo";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        JsonObject jsonObject = new Gson().fromJson(response.getBody(), JsonObject.class);

        String email = jsonObject.get("email").toString().replace("\"", "");
        String name = jsonObject.get("name").toString().replace("\"", "");


        Optional<UserAccount> userOptional = userRepository.findByEmail(email);
        UserAccount user;
        if (userOptional.isEmpty()){
            // Create account if user hasn't sign up yet
            UserAccount newUser = new UserAccount();
            newUser.setFullName(name);
            newUser.setEmail(email);
            newUser.setIsActive(true);
            newUser.setRole(UserAccount.Role.ROLE_JOB_SEEKER);
            newUser.setIsNonLocked(true);
            newUser.setPassword(UUID.randomUUID()+"Salt1");
            user = userRepository.save(newUser);
        }
        else {
            // User has sign up, get their account
            user = userOptional.get();
        }

        String jwtAccessToken = jwtService.generateToken(JwtService.TokenType.ACCESS_TOKEN,user);
        String jwtRefreshToken = jwtService.generateRefreshToken(user);

        user.setRefreshToken(jwtRefreshToken);
        userRepository.save(user);

        return new LoginTokenDto(jwtAccessToken,jwtRefreshToken);
    }

    private String getOauthAccessTokenGoogle(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("redirect_uri", "http://localhost:5173/loginGoogle");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, httpHeaders);

        String url = "https://oauth2.googleapis.com/token";

        // Get the response as a Map to extract the access token
        Map<String, Object> response = restTemplate.postForObject(url, requestEntity, Map.class);

        if (response == null || !response.containsKey("access_token")) {
            throw new IllegalStateException("Failed to retrieve access token from Google");
        }

        return response.get("access_token").toString();
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
