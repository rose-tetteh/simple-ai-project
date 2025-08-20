package com.example.simple_ai_project.service;

import com.example.simple_ai_project.dto.LoginRequest;
import com.example.simple_ai_project.dto.LoginResponse;
import com.example.simple_ai_project.dto.ProfileResponse;
import com.example.simple_ai_project.model.User;
import com.example.simple_ai_project.repository.UserRepository;
import com.example.simple_ai_project.security.JwtUtils;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String USER_NOT_FOUND = "User not found";

    public UserService(AuthenticationManager authenticationManager, JwtUtils jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String signup(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EntityExistsException("Email already exists");
        }
        if (user.getUserName() != null && userRepository.findByUserName(user.getUserName()).isPresent()) {
            throw new EntityExistsException("Username already exists");
        }

        if (user.getUserName() == null || user.getUserName().trim().isEmpty()) {
            user.setUserName(user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User registered successfully";
    }

    public LoginResponse login(LoginRequest authRequest) {
        try {
            User user = userRepository.findByEmail(authRequest.getEmail())
                    .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUserName(), authRequest.getPassword())
            );

            String token = jwtUtil.generateToken(user.getUserName());
            return new LoginResponse(token);
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new EntityNotFoundException("Invalid credentials");
        }
    }

    public ProfileResponse getProfile(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Getting profile for username: {}", username);

        if ("anonymousUser".equals(username)) {
            log.error("User is not authenticated - anonymousUser detected");
            throw new EntityNotFoundException("User not authenticated");
        }

        User user = userRepository.findByUserName(username).orElseThrow(() -> {
            log.error("User not found with username: {}", username);
            return new EntityNotFoundException(USER_NOT_FOUND);
        });
        return new ProfileResponse(user.getEmail(), user.getUserName(), user.getCreatedAt());
    }
}
