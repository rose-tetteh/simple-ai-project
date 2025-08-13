package com.example.simple_ai_project.service;

import com.example.simple_ai_project.dto.LoginRequest;
import com.example.simple_ai_project.dto.LoginResponse;
import com.example.simple_ai_project.model.User;
import com.example.simple_ai_project.repository.UserRepository;
import com.example.simple_ai_project.security.JwtUtils;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtil;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(AuthenticationManager authenticationManager, JwtUtils jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String signup(User user) {
        if (userRepository.findByUserName(user.getEmail()).isPresent()) {
            throw new EntityExistsException("Username already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User registered successfully";
    }

    public LoginResponse login(LoginRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword())
            );
        } catch (Exception e) {
            throw new EntityNotFoundException("Invalid credentials");
        }
        String token = jwtUtil.generateToken(authRequest.getUserName());
        return new LoginResponse(token);
    }
}
