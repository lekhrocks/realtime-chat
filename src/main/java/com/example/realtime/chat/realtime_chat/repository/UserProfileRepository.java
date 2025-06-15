package com.example.realtime.chat.realtime_chat.repository;

import com.example.realtime.chat.realtime_chat.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUsername(String username);
    Optional<UserProfile> findByEmail(String email);
    Optional<UserProfile> findByVerificationToken(String token);
    Optional<UserProfile> findByResetToken(String token);
} 