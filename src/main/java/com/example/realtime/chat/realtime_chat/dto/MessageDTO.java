package com.example.realtime.chat.realtime_chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
    @NotBlank(message = "Sender must not be blank")
    private String from;
    @NotBlank(message = "Recipient must not be blank")
    private String to;
    @NotBlank(message = "Message body must not be blank")
    @Size(max = 500, message = "Message body must be at most 500 characters")
    private String body;
    private String sentAt;
    private String messageType;
}
