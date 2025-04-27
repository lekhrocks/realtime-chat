package com.example.realtime.chat.realtime_chat.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private String username;
    private String password;  // You can encrypt the password for real apps
    private String email;
}
