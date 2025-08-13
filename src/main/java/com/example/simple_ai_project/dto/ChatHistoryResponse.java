package com.example.simple_ai_project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryResponse {
    private String fileName;
    private Double englishProficiencyScore;
    private String pdfLink; // This will hold the pre-signed URL
    private LocalDateTime createdAt;
}
