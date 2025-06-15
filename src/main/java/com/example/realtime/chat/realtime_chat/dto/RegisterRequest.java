package com.example.realtime.chat.realtime_chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class RegisterRequest {
    @Schema(description = "Username", example = "alice")
    public String username;

    @Schema(description = "Email address", example = "alice@example.com")
    public String email;

    @Schema(description = "Password", example = "secret")
    public String password;
} 