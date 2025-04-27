package com.example.realtime.chat.realtime_chat.controller;

import com.example.realtime.chat.realtime_chat.model.UserCredentials;
import com.example.realtime.chat.realtime_chat.security.JwtUtil;
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
    public String login(@RequestBody UserCredentials credentials) {
        // Validate credentials (this can be a DB check)
        if ("user1".equals(credentials.getUsername()) && "password123".equals(credentials.getPassword())) {
            return jwtUtil.generateToken(credentials.getUsername());
        }
        return "Invalid credentials";
    }
}
