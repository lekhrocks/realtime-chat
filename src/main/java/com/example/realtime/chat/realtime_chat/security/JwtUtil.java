package com.example.realtime.chat.realtime_chat.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Component
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

    public String generateToken(String username) {
        JwtBuilder builder = Jwts.builder()
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour expiration
                .signWith(privateKey); // Using Key type for signing

        return builder.compact(); // Build and return the JWT token
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(privateKey)  // Using Key type for verification
                .build()
                .parseClaimsJws(token)
                .getPayload();
    }

    // Method to extract username from JWT token
    public String extractUsername(String token) {
        return extractClaims(token).get("username", String.class);
    }

    // Method to validate the token
    public boolean validateToken(String token, String username) {
        return username.equals(extractUsername(token)) && !isTokenExpired(token);
    }

    // Method to check if the token has expired
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}
