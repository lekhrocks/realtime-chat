package com.example.realtime.chat.realtime_chat.repository;

import com.example.realtime.chat.realtime_chat.model.FileShareNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileShareNotificationRepository extends JpaRepository<FileShareNotification, Long> {
    List<FileShareNotification> findByRecipientId(String recipientId);
    void deleteByIdAndRecipientId(Long id, String recipientId);
    Page<FileShareNotification> findByRecipientId(String recipientId, Pageable pageable);
    Page<FileShareNotification> findByRecipientIdAndRead(String recipientId, boolean read, Pageable pageable);
    Page<FileShareNotification> findByRecipientIdAndMessageContainingIgnoreCase(String recipientId, String message, Pageable pageable);
    long countByRecipientIdAndRead(String recipientId, boolean read);
} 