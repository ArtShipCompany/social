package com.example.artship.social.auth;

import jakarta.validation.constraints.NotBlank;

public class LogoutRequest {
    
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
    
    // Getters and Setters
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}