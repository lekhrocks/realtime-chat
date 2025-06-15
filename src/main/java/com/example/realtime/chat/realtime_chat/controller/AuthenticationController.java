package com.example.realtime.chat.realtime_chat.controller;

import lombok.AllArgsConstructor;
import lombok.Data;

import com.example.realtime.chat.realtime_chat.model.UserCredentials;
import com.example.realtime.chat.realtime_chat.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {
    private final JwtUtil jwtUtil;

    public AuthenticationController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @RequestMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserCredentials credentials) {
        // WARNING: Insecure password comparison! Use hashed passwords and BCrypt in production.
        if ("user1".equals(credentials.getUsername()) && "password123".equals(credentials.getPassword())) {
            String token = jwtUtil.generateToken(credentials.getUsername());
            return ResponseEntity.ok(new AuthResponse(token, "Login successful"));
        }
        return ResponseEntity.status(401).body(new AuthResponse(null, "Invalid credentials"));
    }

    @Data
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String message;
    }
}
