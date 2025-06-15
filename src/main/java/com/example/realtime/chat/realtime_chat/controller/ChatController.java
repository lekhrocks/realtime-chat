package com.example.realtime.chat.realtime_chat.controller;

import com.example.realtime.chat.realtime_chat.dto.PageDTO;
import com.example.realtime.chat.realtime_chat.service.ChatService;
import com.example.realtime.chat.realtime_chat.security.JwtUtil;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {
    private final ChatService chatService;
    private final Sinks.Many<PageDTO> inboxSink = Sinks.many().multicast().onBackpressureBuffer();
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final JwtUtil jwtUtil;
    private final ConcurrentHashMap<String, Long> userLastMessageTimestamps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> userMessageCounts = new ConcurrentHashMap<>();
    private static final int RATE_LIMIT = 5;
    private static final long RATE_LIMIT_WINDOW_MS = 10_000;
    private final RedisTemplate<String, Integer> redisTemplate;

    @Autowired
    public ChatController(ChatService chatService, JwtUtil jwtUtil, RedisTemplate<String, Integer> redisTemplate) {
        this.chatService = chatService;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @QueryMapping
    public String me() {
        return chatService.getMe();
    }

    @MutationMapping
    public PageDTO message(@Argument String body, @Argument String to, @Argument String type, @Argument String token) {
        if (!"text".equals(type) && !"image".equals(type) && !"file".equals(type)) {
            throw new IllegalArgumentException("Only 'text', 'image', and 'file' message types are supported");
        }
        if ("text".equals(type)) {
            if (body == null || body.isBlank() || body.length() > 500) {
                throw new IllegalArgumentException("Message body must be between 1 and 500 characters");
            }
        } else if ("image".equals(type) || "file".equals(type)) {
            if (body == null || !body.startsWith("/api/upload/files/")) {
                throw new IllegalArgumentException("For 'image' and 'file' types, body must be a valid uploaded file URL");
            }
        }
        if (to == null || to.isBlank() || !to.matches("^[a-zA-Z0-9_]{3,30}$")) {
            throw new IllegalArgumentException("Recipient must be alphanumeric (3-30 chars)");
        }
        String username;
        try {
            username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            if (!jwtUtil.validateToken(token.replace("Bearer ", ""), username)) {
                throw new IllegalArgumentException("Invalid or expired token");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        // Persistent rate limiting with Redis
        String rateKey = "rate_limit:" + username;
        ValueOperations<String, Integer> ops = redisTemplate.opsForValue();
        Integer count = ops.get(rateKey);
        if (count == null) {
            ops.set(rateKey, 1, Duration.ofSeconds(10));
        } else if (count >= 5) {
            throw new IllegalArgumentException("Rate limit exceeded. Max 5 messages per 10 seconds.");
        } else {
            ops.increment(rateKey);
        }
        logger.info("Sending message from '{}' to '{}': {}", username, to, body);
        PageDTO page = chatService.sendMessage(body, to, type);
        inboxSink.tryEmitNext(page);
        return page;
    }

    @SubscriptionMapping
    public Publisher<PageDTO> inbox(@Argument String to, @Argument String from, @Argument String token, @Argument String type) {
        String username;
        try {
            username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            if (!jwtUtil.validateToken(token.replace("Bearer ", ""), username)) {
                throw new IllegalArgumentException("Invalid or expired token");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        if (!username.equals(to)) {
            throw new IllegalArgumentException("You can only subscribe to your own inbox");
        }
        logger.info("User '{}' subscribed to inbox from '{}', type '{}'", to, from != null ? from : "any", type != null ? type : "any");
        return inboxSink.asFlux()
            .filter(page -> page.getItems().stream().anyMatch(msg ->
                msg.getTo().equals(to)
                && (from == null || msg.getFrom().equals(from))
                && (type == null || "text".equals(type))
            ))
            .delayElements(Duration.ofMillis(500));
    }

    @QueryMapping
    public PageDTO inboxHistory(@Argument String to) {
        logger.info("Fetching inbox history for user: {}", to);
        return chatService.getInboxForUser(to);
    }

    @QueryMapping
    public PageDTO inboxHistoryPaginated(@Argument String to, @Argument int page, @Argument int size) {
        logger.info("Fetching paginated inbox history for user: {}, page: {}, size: {}", to, page, size);
        return chatService.getInboxForUserPaginated(to, page, size);
    }

    @QueryMapping
    public PageDTO inboxHistoryFiltered(
            @Argument String to,
            @Argument String start,
            @Argument String end,
            @Argument int page,
            @Argument int size) {
        logger.info("Fetching filtered inbox history for user: {}, start: {}, end: {}, page: {}, size: {}", to, start, end, page, size);
        return chatService.getInboxForUserFiltered(to, start, end, page, size);
    }

    @QueryMapping
    public PageDTO inboxHistorySearch(
            @Argument String to,
            @Argument String search,
            @Argument int page,
            @Argument int size) {
        logger.info("Fetching searched inbox history for user: {}, search: '{}', page: {}, size: {}", to, search, page, size);
        return chatService.getInboxForUserSearch(to, search, page, size);
    }

    @QueryMapping
    public PageDTO inboxHistoryCombined(
            @Argument String to,
            @Argument String start,
            @Argument String end,
            @Argument String search,
            @Argument int page,
            @Argument int size) {
        logger.info("Fetching combined filtered inbox history for user: {}, start: {}, end: {}, search: '{}', page: {}, size: {}", to, start, end, search, page, size);
        return chatService.getInboxForUserCombined(to, start, end, search, page, size);
    }
}
