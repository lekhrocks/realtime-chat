package com.example.realtime.chat.realtime_chat.controller;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import com.example.realtime.chat.realtime_chat.service.S3Service;
import com.example.realtime.chat.realtime_chat.service.NotificationService;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private final S3Service s3Service;
    private final NotificationService notificationService;

    @Autowired
    public FileUploadController(S3Service s3Service, NotificationService notificationService) {
        this.s3Service = s3Service;
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam("userId") String userId) {
        try {
            String url = s3Service.uploadFile(file, userId);
            logger.info("File uploaded to S3: {} by user {}", url, userId);
            return ResponseEntity.ok().body(url);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("File upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed");
        }
    }

    @RequestMapping("/signed-url")
    public ResponseEntity<?> getSignedUrl(@RequestParam("fileId") String fileId, @RequestParam("userId") String userId) {
        try {
            String url = s3Service.generateSignedUrl(fileId, userId);
            return ResponseEntity.ok().body(url);
        } catch (Exception e) {
            logger.error("Failed to generate signed URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate signed URL");
        }
    }

    @RequestMapping("/delete")
    public ResponseEntity<?> deleteFile(@RequestParam("fileId") String fileId, @RequestParam("userId") String userId) {
        try {
            s3Service.deleteFile(fileId, userId);
            return ResponseEntity.ok().body("File deleted");
        } catch (Exception e) {
            logger.error("Failed to delete file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete file");
        }
    }

    @RequestMapping("/list")
    public ResponseEntity<?> listFiles(@RequestParam("userId") String userId, @RequestParam(value = "prefix", required = false) String prefix) {
        try {
            return ResponseEntity.ok().body(s3Service.listFiles(userId, prefix));
        } catch (Exception e) {
            logger.error("Failed to list files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to list files");
        }
    }

    @RequestMapping("/quota")
    public ResponseEntity<?> getUserQuota(@RequestParam("userId") String userId) {
        try {
            long total = s3Service.getUserTotalSize(userId);
            return ResponseEntity.ok().body(total);
        } catch (Exception e) {
            logger.error("Failed to get user quota", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get user quota");
        }
    }

    @RequestMapping("/share")
    public ResponseEntity<?> shareFile(@RequestParam("ownerId") String ownerId, @RequestParam("fileId") String fileId, @RequestParam("recipientId") String recipientId, @RequestParam("recipientEmail") String recipientEmail) {
        try {
            s3Service.shareFile(ownerId, fileId, recipientId, recipientEmail);
            return ResponseEntity.ok().body("File shared");
        } catch (Exception e) {
            logger.error("Failed to share file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to share file");
        }
    }

    @RequestMapping("/notifications")
    public ResponseEntity<?> getNotifications(@RequestParam("userId") String userId) {
        try {
            return ResponseEntity.ok().body(s3Service.getNotifications(userId));
        } catch (Exception e) {
            logger.error("Failed to get notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get notifications");
        }
    }

    @RequestMapping("/notifications/page")
    public ResponseEntity<?> getNotificationsPage(@RequestParam("userId") String userId, @RequestParam("page") int page, @RequestParam("size") int size) {
        try {
            return ResponseEntity.ok().body(notificationService.getNotifications(userId, page, size));
        } catch (Exception e) {
            logger.error("Failed to get paginated notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get paginated notifications");
        }
    }

    @RequestMapping("/notifications/unread")
    public ResponseEntity<?> getUnreadNotifications(@RequestParam("userId") String userId, @RequestParam("page") int page, @RequestParam("size") int size) {
        try {
            return ResponseEntity.ok().body(notificationService.getUnreadNotifications(userId, page, size));
        } catch (Exception e) {
            logger.error("Failed to get unread notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get unread notifications");
        }
    }

    @RequestMapping("/notifications/search")
    public ResponseEntity<?> searchNotifications(@RequestParam("userId") String userId, @RequestParam("query") String query, @RequestParam("page") int page, @RequestParam("size") int size) {
        try {
            return ResponseEntity.ok().body(notificationService.searchNotifications(userId, query, page, size));
        } catch (Exception e) {
            logger.error("Failed to search notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to search notifications");
        }
    }

    @RequestMapping("/notifications/mark-all-read")
    public ResponseEntity<?> markAllNotificationsAsRead(@RequestParam("userId") String userId) {
        try {
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok().body("All notifications marked as read");
        } catch (Exception e) {
            logger.error("Failed to mark all notifications as read", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to mark all notifications as read");
        }
    }
} 