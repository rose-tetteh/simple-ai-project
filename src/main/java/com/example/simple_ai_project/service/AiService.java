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

@Service
public class AiService {

    private final OpenAiChatModel openAiChatModel;
    private final PDFGenerator pdfGenerator;
    private final UserRepository userRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final S3Service s3Service;
    private static final String USER_NOT_FOUND = "User not found";

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
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        s3Service.uploadFile(file);

        String analysis;
        try {
            analysis = analyzeFileContent(file.getBytes());
        } catch (IOException e) {
            throw new EntityNotFoundException("File analysis failed: " + e.getMessage());
        }

        String summary = extractSummary(analysis);
        String feedback = extractFeedback(analysis);
        Double score = extractScore(analysis);

        byte[] pdfBytes = pdfGenerator.generatePdf(summary, feedback, score);
        String pdfKey = s3Service.uploadPdf(pdfBytes, Objects.requireNonNull(file.getOriginalFilename()));

        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setUser(user);
        chatHistory.setFileName(file.getOriginalFilename());
        chatHistory.setEnglishProficiencyScore(score);
        chatHistory.setPdfLink(pdfKey);
        chatHistory.setCreatedAt(LocalDateTime.now());
        chatHistoryRepository.save(chatHistory);

        String presignedUrl = s3Service.generatePresignedUrl(pdfKey);
        return new FileProcessingResponse(presignedUrl, score);
    }

    public List<ChatHistoryResponse> getUserChats() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        List<ChatHistory> histories = chatHistoryRepository.findByUser(user);

        return histories.stream().map(history -> new ChatHistoryResponse(
                history.getId(),
                history.getFileName(),
                history.getEnglishProficiencyScore(),
                s3Service.generatePresignedUrl(history.getPdfLink()),
                history.getCreatedAt()
        )).toList();
    }

    public String deleteChatHistory(Long chatId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        ChatHistory chatHistory = chatHistoryRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat history not found"));

        if (!chatHistory.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You are not authorized to delete this chat history");
        }

        try {
            s3Service.deleteFile(chatHistory.getPdfLink());
        } catch (Exception e) {
            throw new EntityNotFoundException("Failed to delete PDF from S3: " + e.getMessage());
        }

        chatHistoryRepository.delete(chatHistory);

        return "Chat history deleted successfully";
    }

    public String analyzeFileContent(byte[] fileContent) {
        String content = new String(fileContent);
        String prompt = "Analyze the following text and provide your response in this exact format:\n\n" +
                "Summary: [Your detailed summary here]\n\n" +
                "Feedback: [Your detailed feedback on English proficiency, grammar, vocabulary, etc.];[Your full modified version based on the feedback]\n\n" +
                "English Proficiency Score: [A number between 0-100]%\n\n" +
                "Text to analyze:\n" + content;
        return openAiChatModel.call(prompt);
    }

    private String extractSummary(String analysis) {
        String[] summaryKeywords = {"Summary:", "SUMMARY:", "summary:", "1. Summary", "**Summary**"};
        String[] feedbackKeywords = {"Feedback:", "FEEDBACK:", "feedback:", "2. Feedback", "**Feedback**"};

        String summaryContent = findContentBetweenKeywords(analysis, summaryKeywords, feedbackKeywords);
        if (summaryContent != null) {
            return summaryContent;
        }

        return getFirstLineOrDefault(analysis);
    }

    private String findContentBetweenKeywords(String analysis, String[] startKeywords, String[] endKeywords) {
        for (String startKeyword : startKeywords) {
            int startIndex = analysis.indexOf(startKeyword);
            if (startIndex != -1) {
                int contentStart = startIndex + startKeyword.length();
                int endIndex = findNextKeywordIndex(analysis, endKeywords, contentStart);

                if (endIndex != -1) {
                    return analysis.substring(contentStart, endIndex).trim();
                } else {
                    return extractRemainingContent(analysis, contentStart);
                }
            }
        }
        return null;
    }

    private int findNextKeywordIndex(String analysis, String[] keywords, int startFrom) {
        for (String keyword : keywords) {
            int index = analysis.indexOf(keyword, startFrom);
            if (index != -1) {
                return index;
            }
        }
        return -1;
    }

    private String extractRemainingContent(String analysis, int startIndex) {
        String remaining = analysis.substring(startIndex);
        int doubleNewline = remaining.indexOf("\n\n");

        if (doubleNewline != -1) {
            return remaining.substring(0, doubleNewline).trim();
        }

        return remaining.length() > 200 ? remaining.substring(0, 200).trim() + "..." : remaining.trim();
    }

    private String getFirstLineOrDefault(String analysis) {
        String[] lines = analysis.split("\n");
        return lines.length > 0 ? lines[0].trim() : "Summary not found";
    }

    private String extractFeedback(String analysis) {
        String[] feedbackKeywords = {"Feedback:", "FEEDBACK:", "feedback:", "2. Feedback", "**Feedback**"};
        String[] scoreKeywords = {"Score:", "SCORE:", "score:", "3. Score", "**Score**", "Percentage:", "Rating:"};

        String feedbackContent = findContentBetweenKeywords(analysis, feedbackKeywords, scoreKeywords);
        if (feedbackContent != null) {
            return feedbackContent;
        }

        return extractFeedbackFromLines(analysis);
    }

    private String extractFeedbackFromLines(String analysis) {
        String[] lines = analysis.split("\n");
        if (lines.length <= 2) {
            return "Feedback not found";
        }

        StringBuilder feedback = new StringBuilder();
        boolean inFeedbackSection = false;

        for (String line : lines) {
            if (shouldStartFeedbackSection(line, inFeedbackSection)) {
                inFeedbackSection = true;
                if (shouldIncludeLine(line)) {
                    feedback.append(line).append("\n");
                } else {
                    break;
                }
            }
        }

        return feedback.isEmpty() ? "Feedback not found" : feedback.toString().trim();
    }

    private boolean shouldStartFeedbackSection(String line, boolean alreadyInSection) {
        String lowerLine = line.toLowerCase();
        return alreadyInSection || lowerLine.contains("feedback") ||
               lowerLine.contains("grammar") || lowerLine.contains("proficiency");
    }

    private boolean shouldIncludeLine(String line) {
        String lowerLine = line.toLowerCase();
        return !lowerLine.contains("score") && !lowerLine.contains("%");
    }

    private Double extractScore(String analysis) {
        String[] scoreKeywords = {"Score:", "SCORE:", "score:", "3. Score", "**Score**", "Percentage:", "Rating:"};

        Double scoreFromKeywords = extractScoreFromKeywords(analysis, scoreKeywords);
        if (scoreFromKeywords != null) {
            return scoreFromKeywords;
        }

        Double scoreFromPercentage = extractScoreFromPercentageWords(analysis);
        return Objects.requireNonNullElse(scoreFromPercentage, 75.0);

    }

    private Double extractScoreFromKeywords(String analysis, String[] scoreKeywords) {
        for (String scoreKeyword : scoreKeywords) {
            int scoreStart = analysis.indexOf(scoreKeyword);
            if (scoreStart != -1) {
                String scoreSection = analysis.substring(scoreStart + scoreKeyword.length()).trim();
                String scoreLine = scoreSection.split("\n")[0].trim();
                Double score = parseScoreFromText(scoreLine);
                if (score != null) {
                    return score;
                }
            }
        }
        return null;
    }

    private Double extractScoreFromPercentageWords(String analysis) {
        String[] words = analysis.split("\\s+");
        for (String word : words) {
            if (word.contains("%")) {
                Double score = parseScoreFromText(word);
                if (score != null) {
                    return score;
                }
            }
        }
        return null;
    }

    private Double parseScoreFromText(String text) {
        String numberStr = text.replaceAll("[^0-9.]", "");
        if (!numberStr.isEmpty()) {
            try {
                double score = Double.parseDouble(numberStr);
                return Math.min(score, 100.0);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
