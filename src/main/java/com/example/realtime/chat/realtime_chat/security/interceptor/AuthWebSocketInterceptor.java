package com.example.realtime.chat.realtime_chat.security.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class AuthWebSocketInterceptor implements HandshakeInterceptor {

    private boolean isValidToken(String token) {
        // Validate the token (you can use JwtUtil here)
        return true;  // For simplicity, always valid
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String token = request.getHeaders().getFirst("Authorization");

        if (token != null && isValidToken(token)) {
            return true; // Allow connection
        }
        return false;  // Reject connection
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
