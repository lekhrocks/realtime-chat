package com.example.realtime.chat.realtime_chat.service;

import com.example.realtime.chat.realtime_chat.codegen.types.Page;
import com.example.realtime.chat.realtime_chat.codegen.types.Message;
import com.example.realtime.chat.realtime_chat.dto.MessageDTO;
import com.example.realtime.chat.realtime_chat.dto.PageDTO;
import com.example.realtime.chat.realtime_chat.model.MessageEntity;
import com.example.realtime.chat.realtime_chat.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private final List<MessageDTO> messages = new CopyOnWriteArrayList<>();
    private final MessageRepository messageRepository;

    @Autowired
    public ChatService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public PageDTO sendMessage(String body, String to, String messageType) {
        logger.info("sendMessage called: from 'me' to '{}', body: {}", to, body);
        MessageEntity entity = MessageEntity.builder()
            .fromUser("me")
            .toUser(to)
            .body(body)
            .sentAt(Instant.now().toString())
            .messageType(messageType)
            .build();
        messageRepository.save(entity);
        MessageDTO message = MessageDTO.builder()
            .from(entity.getFromUser())
            .to(entity.getToUser())
            .body(entity.getBody())
            .sentAt(entity.getSentAt())
            .messageType(entity.getMessageType())
            .build();
        return PageDTO.builder().items(List.of(message)).build();
    }

    public String getMe() {
        return "me";
    }

    public PageDTO getInboxForUser(String toUser) {
        List<MessageEntity> entities = messageRepository.findByToUser(toUser);
        List<MessageDTO> messages = entities.stream()
            .map(entity -> MessageDTO.builder()
                .from(entity.getFromUser())
                .to(entity.getToUser())
                .body(entity.getBody())
                .sentAt(entity.getSentAt())
                .build())
            .toList();
        return PageDTO.builder().items(messages).build();
    }

    public PageDTO getInboxForUserPaginated(String toUser, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        var pageResult = messageRepository.findByToUser(toUser, pageable);
        List<MessageDTO> messages = pageResult.getContent().stream()
            .map(entity -> MessageDTO.builder()
                .from(entity.getFromUser())
                .to(entity.getToUser())
                .body(entity.getBody())
                .sentAt(entity.getSentAt())
                .build())
            .toList();
        return PageDTO.builder().items(messages).build();
    }

    public PageDTO getInboxForUserFiltered(String toUser, String start, String end, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        var pageResult = messageRepository.findByToUserAndSentAtBetween(toUser, start, end, pageable);
        List<MessageDTO> messages = pageResult.getContent().stream()
            .map(entity -> MessageDTO.builder()
                .from(entity.getFromUser())
                .to(entity.getToUser())
                .body(entity.getBody())
                .sentAt(entity.getSentAt())
                .build())
            .toList();
        return PageDTO.builder().items(messages).build();
    }

    public PageDTO getInboxForUserSearch(String toUser, String search, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        var pageResult = messageRepository.findByToUserAndBodyContainingIgnoreCase(toUser, search, pageable);
        List<MessageDTO> messages = pageResult.getContent().stream()
            .map(entity -> MessageDTO.builder()
                .from(entity.getFromUser())
                .to(entity.getToUser())
                .body(entity.getBody())
                .sentAt(entity.getSentAt())
                .build())
            .toList();
        return PageDTO.builder().items(messages).build();
    }

    public PageDTO getInboxForUserCombined(String toUser, String start, String end, String search, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        var pageResult = messageRepository.findByToUserAndSentAtBetweenAndBodyContainingIgnoreCase(toUser, start, end, search, pageable);
        List<MessageDTO> messages = pageResult.getContent().stream()
            .map(entity -> MessageDTO.builder()
                .from(entity.getFromUser())
                .to(entity.getToUser())
                .body(entity.getBody())
                .sentAt(entity.getSentAt())
                .build())
            .toList();
        return PageDTO.builder().items(messages).build();
    }
}
