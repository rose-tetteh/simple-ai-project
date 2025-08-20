package com.example.simple_ai_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ProfileResponse {
    private String email;
    private String userName;
    private LocalDateTime createdAt;
}
