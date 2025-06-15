package com.example.realtime.chat.realtime_chat.model;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCredentials {
    private String username;
    private String password; // WARNING: Store and compare hashed passwords only! Use BCrypt or similar in production.
}
