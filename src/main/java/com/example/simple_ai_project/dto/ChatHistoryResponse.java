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
    private Long id;
    private String fileName;
    private Double englishProficiencyScore;
    private String pdfLink;
    private LocalDateTime createdAt;
}
