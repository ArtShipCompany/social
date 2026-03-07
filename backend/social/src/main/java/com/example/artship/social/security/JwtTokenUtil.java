package com.example.artship.social.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtTokenUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.access-token.expiration}")
    private Long accessTokenExpiration;
    
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    // Исправленный метод с уникальностью
    public String generateAccessToken(CustomUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDetails.getId());
        claims.put("email", userDetails.getEmail());
        
        // ДОБАВЛЯЕМ УНИКАЛЬНОСТЬ!
        claims.put("jti", UUID.randomUUID().toString());  // Unique JWT ID
        claims.put("iat", System.currentTimeMillis());    // Issued at timestamp
        
        Date issuedAt = new Date(System.currentTimeMillis());
        Date expiration = new Date(System.currentTimeMillis() + accessTokenExpiration);
        
        // Дебаг логи
        System.out.println("=== JWT GENERATION DEBUG ===");
        System.out.println("User: " + userDetails.getUsername());
        System.out.println("User ID: " + userDetails.getId());
        System.out.println("Issued at: " + issuedAt);
        System.out.println("Expires at: " + expiration);
        System.out.println("Time to live: " + accessTokenExpiration + " ms");
        System.out.println("Token will expire at: " + expiration.getTime());
        System.out.println("Current time: " + System.currentTimeMillis());
        System.out.println("JTI: " + claims.get("jti"));
        System.out.println("=== END DEBUG ===");
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    // Метод для валидации с проверкой времени
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            
            // Дополнительно проверяем не истек ли токен
            if (isTokenExpired(token)) {
                System.out.println("Token validation: Token is expired!");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("Token validation error: " + e.getMessage());
            return false;
        }
    }
    
    // Метод для явной проверки истечения
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            boolean expired = expiration.before(new Date());
            
            System.out.println("Token expiry check:");
            System.out.println("  Expiration date: " + expiration);
            System.out.println("  Current date: " + new Date());
            System.out.println("  Is expired: " + expired);
            System.out.println("  Time left: " + (expiration.getTime() - System.currentTimeMillis()) + " ms");
            
            return expired;
        } catch (Exception e) {
            System.out.println("Error checking token expiry: " + e.getMessage());
            return true;
        }
    }
    
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }
    
    // Получаем дату истечения
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    // Получаем время выдачи
    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }
    
    // Получаем JTI (JWT ID)
    public String getJtiFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("jti", String.class);
    }
    
    // Получаем время выдачи как timestamp
    public Long getIatFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("iat", Long.class);
    }
    
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}