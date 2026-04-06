package com.example.artship.social.dto;

import com.example.artship.social.model.User;

public class UserFollowInfoDto {
    private Long id;
    private String username;
    private String displayName;
    private String avatarUrl;
    
    public UserFollowInfoDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.displayName = user.getDisplayName();
        this.avatarUrl = user.getAvatarUrl();
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}