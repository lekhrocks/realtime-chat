package com.example.realtime.chat.realtime_chat.controller;

import com.example.realtime.chat.realtime_chat.codegen.types.Page;
import com.example.realtime.chat.realtime_chat.service.ChatService;
import org.reactivestreams.Publisher;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@Controller
public class ChatController {
    private final ChatService chatService;
    private final Sinks.Many<Page> inboxSink = Sinks.many().multicast().onBackpressureBuffer();

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @QueryMapping
    public String me() {
        return chatService.getMe();
    }

    @MutationMapping
    public Page message(@Argument String body, @Argument String to) {
        Page page = chatService.sendMessage(body, to);
        inboxSink.tryEmitNext(page);
        return page;
    }

    @SubscriptionMapping
    public Publisher<Page> inbox(@Argument String to) {
        return inboxSink.asFlux().delayElements(Duration.ofMillis(500));
    }
}
