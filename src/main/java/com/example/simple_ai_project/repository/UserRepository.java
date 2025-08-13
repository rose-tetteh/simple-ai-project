package com.example.simple_ai_project.repository;

import com.example.simple_ai_project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String userEmail);

    Optional<User> findByUserName(String userName);
}
