package com.example.realtime.chat.realtime_chat.controller;

import com.example.realtime.chat.realtime_chat.model.UserProfile;
import com.example.realtime.chat.realtime_chat.service.UserService;
import com.example.realtime.chat.realtime_chat.service.S3Service;
import com.example.realtime.chat.realtime_chat.service.AuditLogService;
import com.example.realtime.chat.realtime_chat.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@Tag(name = "User Management", description = "Endpoints for user registration, authentication, profile, and preferences.")
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private AuditLogService auditLogService;

    @Operation(summary = "Register a new user", description = "Creates a new user and sends a verification email.",
        requestBody = @RequestBody(
            description = "Registration request",
            required = true,
            content = @Content(schema = @Schema(implementation = RegisterRequest.class))
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(schema = @Schema(implementation = com.example.realtime.chat.realtime_chat.model.UserProfile.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = com.example.realtime.chat.realtime_chat.exception.ApiError.class)))
        }
    )
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            UserProfile user = userService.register(request.username, request.email, request.password);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Verify user email", description = "Verifies a user's email using a token.")
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Parameter(description = "Verification token") @RequestParam String token) {
        boolean verified = userService.verifyEmail(token);
        return verified ? ResponseEntity.ok("Email verified") : ResponseEntity.badRequest().body("Invalid token");
    }

    @Operation(summary = "Initiate password reset", description = "Sends a password reset email if the user exists.")
    @PostMapping("/initiate-reset")
    public ResponseEntity<?> initiateReset(@Parameter(description = "User email") @RequestParam String email) {
        userService.initiatePasswordReset(email);
        return ResponseEntity.ok("If the email exists, a reset link has been sent.");
    }

    @Operation(summary = "Reset password", description = "Resets the user's password using a reset token.")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Parameter(description = "Reset token") @RequestParam String token,
                                           @Parameter(description = "New password") @RequestParam String newPassword) {
        boolean reset = userService.resetPassword(token, newPassword);
        return reset ? ResponseEntity.ok("Password reset") : ResponseEntity.badRequest().body("Invalid token");
    }

    @Operation(summary = "Get user profile", description = "Retrieves the profile for a given username.")
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@Parameter(description = "Username") @RequestParam String username) {
        UserProfile user = userService.getProfile(username);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Update user preferences", description = "Updates the user's preferences (JSON string).")
    @PostMapping("/preferences")
    public ResponseEntity<?> updatePreferences(@Parameter(description = "Username") @RequestParam String username,
                                               @Parameter(description = "Preferences JSON") @RequestBody String preferencesJson) {
        userService.updatePreferences(username, preferencesJson);
        return ResponseEntity.ok("Preferences updated");
    }

    @PostMapping("/edit-profile")
    public ResponseEntity<?> editProfile(@RequestParam String username, @RequestParam String email, @RequestParam(required = false) String avatarUrl) {
        userService.updateProfile(username, email, avatarUrl);
        return ResponseEntity.ok("Profile updated");
    }

    @PostMapping("/set-role")
    public ResponseEntity<?> setRole(@RequestParam String adminUsername, @RequestParam String targetUsername, @RequestParam String role) {
        // Pseudo-check: only allow if adminUsername is an admin
        if (!"ADMIN".equals(userService.getProfile(adminUsername).getRole())) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        userService.setRole(targetUsername, role);
        return ResponseEntity.ok("Role updated");
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam String username, @RequestParam("file") MultipartFile file) {
        try {
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("Avatar file too large (max 2MB)");
            }
            String type = file.getContentType();
            if (!"image/png".equals(type) && !"image/jpeg".equals(type)) {
                return ResponseEntity.badRequest().body("Only PNG and JPEG avatars allowed");
            }
            String url = s3Service.uploadFile(file, username);
            userService.updateProfile(username, null, url);
            auditLogService.log(username, "UPLOAD_AVATAR", "Uploaded avatar: " + url);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        // Pseudo-login: check user exists and password matches
        UserProfile user = userService.getProfile(username);
        boolean success = user != null && userService.checkPassword(user, password);
        auditLogService.logLoginAttempt(username, success);
        if (success) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
} 