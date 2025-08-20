package com.example.simple_ai_project.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileProcessingResponse {
    private String pdfPath;
    private Double score;
}