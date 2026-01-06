package com.example.artship.social.dto;

import java.time.LocalDateTime;

import com.example.artship.social.model.User;

public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    
    // Конструктор по умолчанию (БЕЗ параметров)
    public UserDto() {
    }
    
    // Конструктор из User entity
    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.displayName = user.getDisplayName();
        this.avatarUrl = user.getAvatarUrl();
        this.bio = user.getBio();
        this.isPublic = user.getIsPublic();
        this.createdAt = user.getCreatedAt();
    }
    
    // Конструктор копирования
    public UserDto(UserDto other) {
        this.id = other.id;
        this.username = other.username;
        this.email = other.email;
        this.displayName = other.displayName;
        this.avatarUrl = other.avatarUrl;
        this.bio = other.bio;
        this.isPublic = other.isPublic;
        this.createdAt = other.createdAt;
    }

    // Геттеры
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getBio() { return bio; }
    public Boolean getIsPublic() { return isPublic; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    // Сеттеры (нужны для установки значений по отдельности)
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setBio(String bio) { this.bio = bio; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}