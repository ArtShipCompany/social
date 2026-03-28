package com.example.artship.social.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.artship.social.model.User;

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
    
    // Существующий метод для CustomUserDetails
    public String generateAccessToken(CustomUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDetails.getId());
        claims.put("email", userDetails.getEmail());
        claims.put("jti", UUID.randomUUID().toString());
        claims.put("iat", System.currentTimeMillis());
        
        Date issuedAt = new Date(System.currentTimeMillis());
        Date expiration = new Date(System.currentTimeMillis() + accessTokenExpiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("jti", UUID.randomUUID().toString());
        claims.put("iat", System.currentTimeMillis());
        
        Date issuedAt = new Date(System.currentTimeMillis());
        Date expiration = new Date(System.currentTimeMillis() + accessTokenExpiration);
        
        System.out.println("=== Generating new token for user ===");
        System.out.println("Username: " + user.getUsername());
        System.out.println("User ID: " + user.getId());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Expires at: " + expiration);
        System.out.println("JTI: " + claims.get("jti"));
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            
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
    
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            boolean expired = expiration.before(new Date());
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
    
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }
    
    public String getJtiFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("jti", String.class);
    }
    
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