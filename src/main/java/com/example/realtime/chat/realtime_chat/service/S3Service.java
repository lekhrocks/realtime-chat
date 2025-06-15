package com.example.realtime.chat.realtime_chat.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import java.time.Duration;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.realtime.chat.realtime_chat.model.FileShareNotification;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class S3Service {
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;
    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;
    @Value("${cloud.aws.region.static}")
    private String region;
    @Value("${app.s3.bucket}")
    private String bucket;
    @Value("${app.s3.url-prefix}")
    private String urlPrefix;
    @Value("${app.upload.max-size}")
    private long maxSize;
    @Value("${app.upload.allowed-types}")
    private String allowedTypes;
    @Value("${app.s3.signed-url-expiration}")
    private int signedUrlExpiration;
    @Value("${app.upload.user-quota-bytes:52428800}")
    private long userQuotaBytes;

    private S3Client s3Client;
    private S3Presigner s3Presigner;
    private final ConcurrentHashMap<String, ArrayList<FileShareNotification>> notifications = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sharedFileOwners = new ConcurrentHashMap<>();

    @Autowired
    private NotificationService notificationService;

    @PostConstruct
    public void init() {
        s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
        s3Presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    public String uploadFile(MultipartFile file, String userId) throws IOException {
        // Per-user quota check
        long totalSize = getUserTotalSize(userId);
        if (totalSize + file.getSize() > userQuotaBytes) {
            throw new IllegalArgumentException("User quota exceeded (" + userQuotaBytes / (1024 * 1024) + " MB)");
        }
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds max allowed size");
        }
        String contentType = file.getContentType();
        if (contentType == null || Arrays.stream(allowedTypes.split(",")).noneMatch(contentType::equals)) {
            throw new IllegalArgumentException("File type not allowed");
        }
        String ext = URLConnection.guessContentTypeFromName(file.getOriginalFilename());
        String fileId = userId + "/" + UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(fileId)
                            .contentType(contentType)
                            .build(),
                    software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));
            logger.info("[AUDIT] User '{}' uploaded file: {}", userId, fileId);
        } catch (S3Exception e) {
            logger.error("[AUDIT] S3 upload failed for user '{}': {}", userId, e.awsErrorDetails().errorMessage());
            throw new IOException("S3 upload failed: " + e.awsErrorDetails().errorMessage(), e);
        }
        return urlPrefix + fileId;
    }

    public void deleteFile(String fileId, String userId) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(fileId)
                .build());
        logger.info("[AUDIT] User '{}' deleted file: {}", userId, fileId);
    }

    public List<String> listFiles(String userId, String prefix) {
        String userPrefix = userId + "/" + (prefix == null ? "" : prefix);
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(userPrefix)
                .build();
        List<String> files = s3Client.listObjectsV2(request).contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
        logger.info("[AUDIT] User '{}' listed files with prefix '{}': {}", userId, userPrefix, files.size());
        return files;
    }

    public String generateSignedUrl(String fileId, String userId) {
        // Access control: only owner or recipient can access
        if (!fileId.startsWith(userId + "/") && !sharedFileOwners.getOrDefault(fileId, "").equals(userId)) {
            throw new SecurityException("Access denied");
        }
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(fileId)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(signedUrlExpiration))
                .getObjectRequest(getObjectRequest)
                .build();
        String url = s3Presigner.presignGetObject(presignRequest).url().toString();
        logger.info("[AUDIT] User '{}' generated signed URL for file: {}", userId, fileId);
        return url;
    }

    public long getUserTotalSize(String userId) {
        String userPrefix = userId + "/";
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(userPrefix)
                .build();
        return s3Client.listObjectsV2(request).contents().stream()
                .mapToLong(S3Object::size)
                .sum();
    }

    public void shareFile(String ownerId, String fileId, String recipientId, String recipientEmail) {
        String destKey = recipientId + "/shared-" + fileId.substring(fileId.indexOf('/') + 1);
        CopyObjectRequest copyReq = CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(fileId)
                .destinationBucket(bucket)
                .destinationKey(destKey)
                .build();
        s3Client.copyObject(copyReq);
        logger.info("[AUDIT] User '{}' shared file '{}' with user '{}'. New key: {}", ownerId, fileId, recipientId, destKey);
        // Store notification
        FileShareNotification notif = FileShareNotification.builder()
                .senderId(ownerId)
                .recipientId(recipientId)
                .fileId(destKey)
                .message("File shared with you: " + destKey)
                .timestamp(Instant.now().toString())
                .build();
        notifications.computeIfAbsent(recipientId, k -> new ArrayList<>()).add(notif);
        sharedFileOwners.put(destKey, ownerId);
        // Send notifications
        notificationService.sendEmailNotification(recipientEmail, notif);
        notificationService.sendWebSocketNotification(recipientId, notif);
    }

    public ArrayList<FileShareNotification> getNotifications(String userId) {
        return notifications.getOrDefault(userId, new ArrayList<>());
    }
} 