package com.banking.auth_service.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // Secret key banao — HMAC-SHA256 algorithm ke liye
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Token generate karo — login/register ke baad yeh call hoga
    public String generateToken(String email, String role, String name) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("name", name);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email) // email = token ka subject
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Token se email nikalo
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // Token se role nikalo
    public String extractRole(String token) {
        return (String) getClaims(token).get("role");
    }

    // Token valid hai ya nahi check karo
    public boolean isTokenValid(String token) {
        try {
            getClaims(token); // exception aayega agar invalid/expired
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Token expire hua hai?
    public boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
