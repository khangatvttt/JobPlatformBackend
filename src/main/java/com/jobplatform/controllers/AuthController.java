package com.jobplatform.controllers;

import com.jobplatform.models.UserAccount;
import com.jobplatform.models.dto.LoginDto;
import com.jobplatform.models.dto.LoginTokenDto;
import com.jobplatform.services.AccountService;
import com.jobplatform.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/auth")
@RestController
public class AuthController {

    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> register(HttpServletRequest request,@Valid @RequestBody UserAccount user) {
        accountService.signUp(user, getBaseURL(request));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginTokenDto> login(@RequestBody LoginDto loginInfo) {
        LoginTokenDto loginToken = accountService.login(loginInfo);
        return new ResponseEntity<>(loginToken, HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginTokenDto> refreshToken(@RequestBody Map<String, String> payload) {
        String refreshToken = payload.get("refreshToken");
        LoginTokenDto loginToken = accountService.refresh(refreshToken);
        return new ResponseEntity<>(loginToken, HttpStatus.OK);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<LoginTokenDto> verifyEmail(@RequestParam String token) {
        accountService.verifyEmail(token);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/reset-password-request")
    public ResponseEntity<LoginTokenDto> requestResetPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        accountService.sendResetPasswordEmail(email);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<LoginTokenDto> resetPassword(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String password = payload.get("password");
        accountService.resetPassword(token, password);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private String getBaseURL(HttpServletRequest request){
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String baseURL;
        if ((serverPort == 80 && request.getScheme().equals("http")) ||
                (serverPort == 443 && request.getScheme().equals("https"))) {
            baseURL = serverName;
        } else {
            baseURL = serverName + ":" + serverPort;
        }
        return baseURL;
    }
}
