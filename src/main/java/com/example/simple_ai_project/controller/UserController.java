package com.example.simple_ai_project.controller;

import com.example.simple_ai_project.dto.LoginRequest;
import com.example.simple_ai_project.dto.LoginResponse;
import com.example.simple_ai_project.dto.ResponseHandler;
import com.example.simple_ai_project.model.User;
import com.example.simple_ai_project.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/auth/hi")
    public ResponseEntity<String> sayHi(){
        return ResponseEntity.ok("Hiiiiiiiiiiiiiiiiiiiiiiii");
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<Object> signup(@RequestBody User user) {
        try {
            String result = userService.signup(user);
            return ResponseHandler.success(null, result, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseHandler.error(null, e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest authRequest) {
        try {
            LoginResponse loginResponse = userService.login(authRequest);
            return ResponseHandler.success(loginResponse, "Login successful", HttpStatus.OK);
        } catch (Exception e) {
            return ResponseHandler.error(null, e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
}
