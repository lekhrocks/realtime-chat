package com.example.realtime.chat.realtime_chat.controller;

import com.example.realtime.chat.realtime_chat.repository.UserProfileRepository;
import com.example.realtime.chat.realtime_chat.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserProfileRepository userRepo;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String search) {
        model.addAttribute("users", userRepo.findAll());
        if (search != null && !search.isEmpty()) {
            model.addAttribute("auditLogs", auditLogService.searchLogs(search, page, size));
        } else {
            model.addAttribute("auditLogs", auditLogService.getLogsPage(page, size));
        }
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("search", search);
        // Add more attributes for files, quotas, health as needed
        return "admin/dashboard";
    }
} 