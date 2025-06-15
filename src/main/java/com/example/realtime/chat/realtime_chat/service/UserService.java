package com.example.realtime.chat.realtime_chat.service;

import com.example.realtime.chat.realtime_chat.model.UserProfile;
import com.example.realtime.chat.realtime_chat.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserProfileRepository userRepo;
    @Autowired
    private JavaMailSender mailSender;
    @Value("${app.notification.email-from}")
    private String emailFrom;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserProfile register(String username, String email, String password) {
        if (userRepo.findByUsername(username).isPresent() || userRepo.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Username or email already exists");
        }
        String token = UUID.randomUUID().toString();
        UserProfile user = UserProfile.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .emailVerified(false)
                .verificationToken(token)
                .role("USER")
                .build();
        userRepo.save(user);
        sendVerificationEmail(user);
        return user;
    }

    public void sendVerificationEmail(UserProfile user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailFrom);
        message.setTo(user.getEmail());
        message.setSubject("Verify your email");
        message.setText("Please verify your email using this token: " + user.getVerificationToken());
        mailSender.send(message);
    }

    public boolean verifyEmail(String token) {
        Optional<UserProfile> userOpt = userRepo.findByVerificationToken(token);
        if (userOpt.isPresent()) {
            UserProfile user = userOpt.get();
            user.setEmailVerified(true);
            user.setVerificationToken(null);
            userRepo.save(user);
            return true;
        }
        return false;
    }

    public void initiatePasswordReset(String email) {
        Optional<UserProfile> userOpt = userRepo.findByEmail(email);
        if (userOpt.isPresent()) {
            UserProfile user = userOpt.get();
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            userRepo.save(user);
            sendResetEmail(user);
        }
    }

    public void sendResetEmail(UserProfile user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailFrom);
        message.setTo(user.getEmail());
        message.setSubject("Password reset");
        message.setText("Reset your password using this token: " + user.getResetToken());
        mailSender.send(message);
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<UserProfile> userOpt = userRepo.findByResetToken(token);
        if (userOpt.isPresent()) {
            UserProfile user = userOpt.get();
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            user.setResetToken(null);
            userRepo.save(user);
            return true;
        }
        return false;
    }

    public void updatePreferences(String username, String preferencesJson) {
        Optional<UserProfile> userOpt = userRepo.findByUsername(username);
        if (userOpt.isPresent()) {
            UserProfile user = userOpt.get();
            user.setPreferencesJson(preferencesJson);
            userRepo.save(user);
        }
    }

    public UserProfile getProfile(String username) {
        return userRepo.findByUsername(username).orElse(null);
    }

    public void updateProfile(String username, String email, String avatarUrl) {
        Optional<UserProfile> userOpt = userRepo.findByUsername(username);
        if (userOpt.isPresent()) {
            UserProfile user = userOpt.get();
            user.setEmail(email);
            user.setAvatarUrl(avatarUrl);
            userRepo.save(user);
        }
    }

    public void setRole(String username, String role) {
        Optional<UserProfile> userOpt = userRepo.findByUsername(username);
        if (userOpt.isPresent()) {
            UserProfile user = userOpt.get();
            user.setRole(role);
            userRepo.save(user);
        }
    }

    public boolean checkPassword(UserProfile user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }
}
