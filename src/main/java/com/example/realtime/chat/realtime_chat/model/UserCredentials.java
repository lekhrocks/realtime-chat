package com.example.realtime.chat.realtime_chat.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCredentials {
    private String username;
    private String password;
}
