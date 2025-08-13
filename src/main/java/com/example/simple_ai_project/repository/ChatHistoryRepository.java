package com.example.simple_ai_project.repository;

import com.example.simple_ai_project.model.ChatHistory;
import com.example.simple_ai_project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    List<ChatHistory> findByUser(User user);
}
