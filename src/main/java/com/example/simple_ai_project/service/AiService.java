package com.example.simple_ai_project.service;

import com.example.simple_ai_project.dto.ChatHistoryResponse;
import com.example.simple_ai_project.dto.FileProcessingResponse;
import com.example.simple_ai_project.model.ChatHistory;
import com.example.simple_ai_project.model.User;
import com.example.simple_ai_project.repository.ChatHistoryRepository;
import com.example.simple_ai_project.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AiService {

    private final OpenAiChatModel openAiChatModel;
    private final PDFGenerator pdfGenerator;
    private final UserRepository userRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final S3Service s3Service;

    public AiService(OpenAiChatModel openAiChatModel, PDFGenerator pdfGenerator, UserRepository userRepository, ChatHistoryRepository chatHistoryRepository, S3Service s3Service) {
        this.openAiChatModel = openAiChatModel;
        this.pdfGenerator = pdfGenerator;
        this.userRepository = userRepository;
        this.chatHistoryRepository = chatHistoryRepository;
        this.s3Service = s3Service;
    }

    public FileProcessingResponse processFile(MultipartFile file) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Upload original file to S3
        String fileKey = s3Service.uploadFile(file);

        // Analyze file content
        String analysis;
        try {
            analysis = analyzeFileContent(file.getBytes());
        } catch (IOException e) {
            throw new EntityNotFoundException("File analysis failed: " + e.getMessage());
        }

        String summary = extractSummary(analysis);
        String feedback = extractFeedback(analysis);
        Double score = extractScore(analysis);

        // Generate PDF and upload to S3
        byte[] pdfBytes = pdfGenerator.generatePdf(summary, feedback, score);
        String pdfKey = s3Service.uploadPdf(pdfBytes, Objects.requireNonNull(file.getOriginalFilename()));

        // Save chat history with S3 key for the PDF
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setUser(user);
        chatHistory.setFileName(file.getOriginalFilename());
        chatHistory.setEnglishProficiencyScore(score);
        chatHistory.setPdfLink(pdfKey); // Save the S3 key
        chatHistory.setCreatedAt(LocalDateTime.now());
        chatHistoryRepository.save(chatHistory);

        // Generate pre-signed URL for the response
        String presignedUrl = s3Service.generatePresignedUrl(pdfKey);
        return new FileProcessingResponse(presignedUrl, score);
    }

    public List<ChatHistoryResponse> getUserChats() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ChatHistory> histories = chatHistoryRepository.findByUser(user);

        return histories.stream().map(history -> new ChatHistoryResponse(
                history.getFileName(),
                history.getEnglishProficiencyScore(),
                s3Service.generatePresignedUrl(history.getPdfLink()), // Generate pre-signed URL
                history.getCreatedAt()
        )).collect(Collectors.toList());
    }

    public String analyzeFileContent(byte[] fileContent) throws IOException {
        String content = new String(fileContent);
        String prompt = "Analyze the following text and provide your response in this exact format:\n\n" +
                "Summary: [Your detailed summary here]\n\n" +
                "Feedback: [Your detailed feedback on English proficiency, grammar, vocabulary, etc.];[Your full modified version based on the feedback]\n\n" +
                "Score: [A number between 0-100]%\n\n" +
                "Text to analyze:\n" + content;
        return openAiChatModel.call(prompt);
    }

    // extractSummary, extractFeedback, and extractScore methods remain the same
    private String extractSummary(String analysis) {
        String[] summaryKeywords = {"Summary:", "SUMMARY:", "summary:", "1. Summary", "**Summary**"};
        String[] feedbackKeywords = {"Feedback:", "FEEDBACK:", "feedback:", "2. Feedback", "**Feedback**"};
        for (String summaryKeyword : summaryKeywords) {
            int summaryStart = analysis.indexOf(summaryKeyword);
            if (summaryStart != -1) {
                int contentStart = summaryStart + summaryKeyword.length();
                int feedbackStart = -1;
                for (String feedbackKeyword : feedbackKeywords) {
                    feedbackStart = analysis.indexOf(feedbackKeyword, contentStart);
                    if (feedbackStart != -1) break;
                }
                if (feedbackStart != -1) {
                    return analysis.substring(contentStart, feedbackStart).trim();
                } else {
                    String remaining = analysis.substring(contentStart);
                    int doubleNewline = remaining.indexOf("\n\n");
                    if (doubleNewline != -1) {
                        return remaining.substring(0, doubleNewline).trim();
                    } else {
                        return remaining.length() > 200 ? remaining.substring(0, 200).trim() + "..." : remaining.trim();
                    }
                }
            }
        }
        String[] lines = analysis.split("\n");
        if (lines.length > 0) {
            return lines[0].trim();
        }
        return "Summary not found";
    }

    private String extractFeedback(String analysis) {
        String[] feedbackKeywords = {"Feedback:", "FEEDBACK:", "feedback:", "2. Feedback", "**Feedback**"};
        String[] scoreKeywords = {"Score:", "SCORE:", "score:", "3. Score", "**Score**", "Percentage:", "Rating:"};
        for (String feedbackKeyword : feedbackKeywords) {
            int feedbackStart = analysis.indexOf(feedbackKeyword);
            if (feedbackStart != -1) {
                int contentStart = feedbackStart + feedbackKeyword.length();
                int scoreStart = -1;
                for (String scoreKeyword : scoreKeywords) {
                    scoreStart = analysis.indexOf(scoreKeyword, contentStart);
                    if (scoreStart != -1) break;
                }
                if (scoreStart != -1) {
                    return analysis.substring(contentStart, scoreStart).trim();
                } else {
                    return analysis.substring(contentStart).trim();
                }
            }
        }
        String[] lines = analysis.split("\n");
        if (lines.length > 2) {
            StringBuilder feedback = new StringBuilder();
            boolean inFeedbackSection = false;
            for (String line : lines) {
                if (line.toLowerCase().contains("feedback") || line.toLowerCase().contains("grammar") ||
                        line.toLowerCase().contains("proficiency") || inFeedbackSection) {
                    inFeedbackSection = true;
                    if (!line.toLowerCase().contains("score") && !line.toLowerCase().contains("%")) {
                        feedback.append(line).append("\n");
                    } else {
                        break;
                    }
                }
            }
            if (!feedback.isEmpty()) {
                return feedback.toString().trim();
            }
        }
        return "Feedback not found";
    }

    private Double extractScore(String analysis) {
        String[] scoreKeywords = {"Score:", "SCORE:", "score:", "3. Score", "**Score**", "Percentage:", "Rating:"};
        for (String scoreKeyword : scoreKeywords) {
            int scoreStart = analysis.indexOf(scoreKeyword);
            if (scoreStart != -1) {
                String scoreSection = analysis.substring(scoreStart + scoreKeyword.length()).trim();
                String[] lines = scoreSection.split("\n");
                String scoreLine = lines[0].trim();
                String numberStr = scoreLine.replaceAll("[^0-9.]", "");
                if (!numberStr.isEmpty()) {
                    try {
                        double score = Double.parseDouble(numberStr);
                        return Math.min(score, 100.0);
                    } catch (NumberFormatException e) {
                        // Continue
                    }
                }
            }
        }
        String[] words = analysis.split("\\s+");
        for (String word : words) {
            if (word.contains("%")) {
                String numberStr = word.replaceAll("[^0-9.]", "");
                if (!numberStr.isEmpty()) {
                    try {
                        double score = Double.parseDouble(numberStr);
                        return Math.min(score, 100.0);
                    } catch (NumberFormatException e) {
                        // Continue
                    }
                }
            }
        }
        return 75.0;
    }
}
