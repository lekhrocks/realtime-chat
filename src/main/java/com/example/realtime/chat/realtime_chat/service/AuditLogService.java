package com.example.realtime.chat.realtime_chat.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuditLogEntry {
        private String timestamp;
        private String user;
        private String action;
        private String details;
    }

    private final List<AuditLogEntry> logs = Collections.synchronizedList(new ArrayList<>());

    public void log(String user, String action, String details) {
        logs.add(new AuditLogEntry(Instant.now().toString(), user, action, details));
    }

    public List<AuditLogEntry> getLogs() {
        return new ArrayList<>(logs);
    }

    public List<AuditLogEntry> getLogsPage(int page, int size) {
        int from = page * size;
        int to = Math.min(from + size, logs.size());
        if (from >= logs.size()) return new ArrayList<>();
        return logs.subList(from, to);
    }

    public List<AuditLogEntry> searchLogs(String query, int page, int size) {
        List<AuditLogEntry> filtered = logs.stream()
            .filter(log -> log.getUser().contains(query) || log.getAction().contains(query) || log.getDetails().contains(query))
            .collect(Collectors.toList());
        int from = page * size;
        int to = Math.min(from + size, filtered.size());
        if (from >= filtered.size()) return new ArrayList<>();
        return filtered.subList(from, to);
    }

    public void logFileAction(String user, String action, String fileId) {
        log(user, action, "File: " + fileId);
    }

    public void logLoginAttempt(String user, boolean success) {
        log(user, "LOGIN_ATTEMPT", success ? "Success" : "Failure");
    }
} 