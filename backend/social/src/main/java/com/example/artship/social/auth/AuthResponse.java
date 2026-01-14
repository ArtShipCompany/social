package com.example.artship.social.auth;

import com.example.artship.social.dto.UserDto;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;  // ← Это должно быть оставшееся время в миллисекундах
    private UserDto user;
    
    public AuthResponse(String accessToken, String refreshToken, long expiresIn, UserDto user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;  // ← Оставшееся время жизни
        this.user = user;
    }
    
    // Геттеры и сеттеры
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
    
    public UserDto getUser() { return user; }
    public void setUser(UserDto user) { this.user = user; }
}