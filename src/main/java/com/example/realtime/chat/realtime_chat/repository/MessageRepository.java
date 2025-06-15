package com.example.realtime.chat.realtime_chat.repository;

import com.example.realtime.chat.realtime_chat.model.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    List<MessageEntity> findByToUser(String toUser);
    Page<MessageEntity> findByToUser(String toUser, Pageable pageable);
    Page<MessageEntity> findByToUserAndSentAtBetween(String toUser, String start, String end, Pageable pageable);
    Page<MessageEntity> findByToUserAndBodyContainingIgnoreCase(String toUser, String body, Pageable pageable);
    Page<MessageEntity> findByToUserAndSentAtBetweenAndBodyContainingIgnoreCase(String toUser, String start, String end, String body, Pageable pageable);
} 