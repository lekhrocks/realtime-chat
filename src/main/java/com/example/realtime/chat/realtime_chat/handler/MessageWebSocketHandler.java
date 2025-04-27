package com.example.realtime.chat.realtime_chat.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class MessageWebSocketHandler extends TextWebSocketHandler {
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Process incoming message and send it to the target user
        String responseMessage = "Message received: " + message.getPayload();
        session.sendMessage(new TextMessage(responseMessage));
    }
}
