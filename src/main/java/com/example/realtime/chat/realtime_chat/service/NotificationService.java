package com.example.realtime.chat.realtime_chat.service;

import com.example.realtime.chat.realtime_chat.model.FileShareNotification;
import com.example.realtime.chat.realtime_chat.repository.FileShareNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Value("${app.notification.email-from}")
    private String emailFrom;
    @Autowired
    private FileShareNotificationRepository notificationRepo;

    // In-memory map for notification read status: userId -> notificationId -> read
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>> readStatus = new ConcurrentHashMap<>();

    public void sendEmailNotification(String toEmail, FileShareNotification notification) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailFrom);
        message.setTo(toEmail);
        message.setSubject("A file was shared with you");
        message.setText(notification.getMessage() + "\nFile ID: " + notification.getFileId());
        mailSender.send(message);
    }

    public void sendWebSocketNotification(String userId, FileShareNotification notification) {
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
    }

    public void markAsRead(String userId, String notificationId) {
        readStatus.computeIfAbsent(userId, k -> new ConcurrentHashMap<>()).put(notificationId, true);
    }

    public boolean isRead(String userId, String notificationId) {
        return readStatus.getOrDefault(userId, new ConcurrentHashMap<>()).getOrDefault(notificationId, false);
    }

    public List<FileShareNotification> getNotifications(String userId) {
        return notificationRepo.findByRecipientId(userId);
    }

    @Transactional
    public void markAsRead(String userId, Long notificationId) {
        FileShareNotification notif = notificationRepo.findById(notificationId).orElse(null);
        if (notif != null && notif.getRecipientId().equals(userId)) {
            notif.setRead(true);
            notificationRepo.save(notif);
        }
    }

    @Transactional
    public void deleteNotification(String userId, Long notificationId) {
        notificationRepo.deleteByIdAndRecipientId(notificationId, userId);
    }

    public Page<FileShareNotification> getNotifications(String userId, int page, int size) {
        return notificationRepo.findByRecipientId(userId, PageRequest.of(page, size));
    }

    public Page<FileShareNotification> getUnreadNotifications(String userId, int page, int size) {
        return notificationRepo.findByRecipientIdAndRead(userId, false, PageRequest.of(page, size));
    }

    public Page<FileShareNotification> searchNotifications(String userId, String query, int page, int size) {
        return notificationRepo.findByRecipientIdAndMessageContainingIgnoreCase(userId, query, PageRequest.of(page, size));
    }

    @Transactional
    public void markAllAsRead(String userId) {
        List<FileShareNotification> notifs = notificationRepo.findByRecipientId(userId);
        for (FileShareNotification notif : notifs) {
            notif.setRead(true);
        }
        notificationRepo.saveAll(notifs);
    }
} 