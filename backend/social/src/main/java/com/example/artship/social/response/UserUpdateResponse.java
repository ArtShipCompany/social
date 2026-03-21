package com.example.artship.social.response;


import com.example.artship.social.dto.UserDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after user profile update")
public class UserUpdateResponse {
    
    @Schema(description = "Updated user data")
    private UserDto user;
    
    @Schema(description = "New JWT token (only if username was changed)")
    private String newToken;
    
    @Schema(description = "Flag indicating if username was changed")
    private boolean usernameChanged;
    
    public UserUpdateResponse(UserDto user) {
        this.user = user;
        this.usernameChanged = false;
        this.newToken = null;
    }
    
    public UserUpdateResponse(UserDto user, String newToken) {
        this.user = user;
        this.newToken = newToken;
        this.usernameChanged = true;
    }
    
    // Getters
    public UserDto getUser() {
        return user;
    }
    
    public String getNewToken() {
        return newToken;
    }
    
    public boolean isUsernameChanged() {
        return usernameChanged;
    }
    
    // Setters
    public void setUser(UserDto user) {
        this.user = user;
    }
    
    public void setNewToken(String newToken) {
        this.newToken = newToken;
    }
    
    public void setUsernameChanged(boolean usernameChanged) {
        this.usernameChanged = usernameChanged;
    }
}