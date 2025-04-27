package com.example.realtime.chat.realtime_chat.service;

import com.example.realtime.chat.realtime_chat.security.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final JwtUtil jwtUtil;

    public UserService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String registerUser(String username, String password, String email) {
        // Add user to in-memory DB or actual DB
        // Here you can perform validation and add them to your DB or cache.
        return "User registered successfully!";
    }

    public String loginUser(String username, String password) {
        // Validate user credentials and generate token
        return jwtUtil.generateToken(username);
    }
}
