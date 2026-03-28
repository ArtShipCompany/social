package com.example.artship.social.model.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "tokens")
public class VerificationToken {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String token;
    
    @Indexed
    private Long userId;
    
    private TokenType type;
    
    private LocalDateTime createdAt;
    
    @Indexed(expireAfterSeconds = 0)
    private LocalDateTime expiryDate;
    
    private boolean used;
    
    public enum TokenType {
        EMAIL_VERIFICATION,
        PASSWORD_RESET,
        EMAIL_CHANGE
    }
    
    public VerificationToken() {}
    
    public VerificationToken(String token, Long userId, TokenType type, LocalDateTime expiryDate) {
        this.token = token;
        this.userId = userId;
        this.type = type;
        this.expiryDate = expiryDate;
        this.createdAt = LocalDateTime.now();
        this.used = false;
    }
    
    public boolean isValid() {
        return !used && expiryDate != null && expiryDate.isAfter(LocalDateTime.now());
    }
    
    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public TokenType getType() { return type; }
    public void setType(TokenType type) { this.type = type; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}