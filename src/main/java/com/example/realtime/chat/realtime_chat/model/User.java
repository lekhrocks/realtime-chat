package com.example.realtime.chat.realtime_chat.model;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String username;
    private String password;  // WARNING: Store hashed passwords only! Use BCrypt or similar in production.
    private String email;
}
