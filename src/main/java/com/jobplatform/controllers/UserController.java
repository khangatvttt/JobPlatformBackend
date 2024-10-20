package com.jobplatform.controllers;

import com.jobplatform.models.UserAccount;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/")
    public ResponseEntity<String> protectedTest() {
        return new ResponseEntity<String>("OK nha",HttpStatus.OK );
    }
}
