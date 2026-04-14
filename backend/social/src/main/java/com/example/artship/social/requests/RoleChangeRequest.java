package com.example.artship.social.requests;

import com.example.artship.social.model.UserRole;
import jakarta.validation.constraints.NotNull;

public class RoleChangeRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "New role is required")
    private UserRole newRole;
    
    private String username;
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public UserRole getNewRole() {
        return newRole;
    }
    
    public void setNewRole(UserRole newRole) {
        this.newRole = newRole;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
}