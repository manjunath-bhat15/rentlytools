package com.rentlytools.backend.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class JwtService {

    private final String secret = "RentlyTools_Long_JWT_SECRET_KEY_9876543211234567";
    private final long expiryMs = 1000 * 60 * 60 * 24; // 1 day

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                .compact();
    }
}
