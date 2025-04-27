package com.example.realtime.chat.realtime_chat.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DummyRestController {
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
