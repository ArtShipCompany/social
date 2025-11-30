package com.example.artship.social.dto;

import java.time.LocalDateTime;

import com.example.artship.social.model.User;

public class UserDto {
    private Long id;
    private String username;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    
    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.displayName = user.getDisplayName();
        this.avatarUrl = user.getAvatarUrl();
        this.bio = user.getBio();
        this.isPublic = user.getIsPublic();
        this.createdAt = user.getCreatedAt();
    }
    
    public UserDto(UserDto other) {
        this.id = other.id;
        this.username = other.username;
        this.displayName = other.displayName;
        this.avatarUrl = other.avatarUrl;
        this.bio = other.bio;
        this.isPublic = other.isPublic;
        this.createdAt = other.createdAt;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getBio() { return bio; }
    public Boolean getIsPublic() { return isPublic; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}