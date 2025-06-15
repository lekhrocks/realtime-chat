package com.example.chat.realtime_chat.security;

import org.springframework.beans.factory.annotation.Value;
import javax.annotation.PostConstruct;
import javax.crypto.spec.SecretKeySpec;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;

public class JwtUtil {
    private Key privateKey;
    private final String secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secretKey) {
        this.secretKey = secretKey;
    }

    @PostConstruct
    public void init() {
        this.privateKey = new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS256.getJcaName());
    }
}
