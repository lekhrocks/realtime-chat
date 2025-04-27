package com.example.realtime.chat.realtime_chat.service;

import com.example.realtime.chat.realtime_chat.codegen.types.Page;
import com.example.realtime.chat.realtime_chat.codegen.types.Message;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ChatService {
    private final List<Message> messages = new CopyOnWriteArrayList<>();

    public Page sendMessage(String body, String to) {
        Message message = new Message("me", to, body, Instant.now().toString());
        messages.add(message);
        return new Page(List.of(message));
    }

    public String getMe() {
        return "me";
    }
}
