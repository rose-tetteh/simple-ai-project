package com.example.simple_ai_project.controller;

import com.example.simple_ai_project.dto.*;
import com.example.simple_ai_project.model.User;
import com.example.simple_ai_project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/auth/hi")
    public ResponseEntity<String> sayHi(){
        return ResponseEntity.ok("CORS is working! Backend is running on port 8087");
    }

    @GetMapping("/auth/test-cors")
    public ResponseEntity<Object> testCors(){
        return ResponseHandler.success("CORS test successful", "Backend accessible from frontend", HttpStatus.OK);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<Object> register(@RequestBody RegisterRequest registerRequest) {
        try {
            User user = User.builder()
                    .email(registerRequest.getEmail())
                    .userName(registerRequest.getUserName())
                    .password(registerRequest.getPassword())
                    .build();

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

    @GetMapping("/users/profile")
    public ResponseEntity<Object> getProfile() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            log.debug("Authentication object: {}", auth);
            log.debug("Is authenticated: {}", auth != null && auth.isAuthenticated());
            log.debug("Principal: {}", auth != null ? auth.getName() : "null");

            ProfileResponse user = userService.getProfile();
            return ResponseHandler.success(user, "Profile retrieved successfully", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error getting profile: {}", e.getMessage());
            return ResponseHandler.error(null, e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/users/auth-test")
    public ResponseEntity<Object> testAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseHandler.success(
            auth != null ? auth.getName() : "No authentication",
            "Auth test",
            HttpStatus.OK
        );
    }
}
