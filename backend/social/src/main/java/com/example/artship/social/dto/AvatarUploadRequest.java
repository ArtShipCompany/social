package com.example.artship.social.dto;


public class AvatarUploadRequest {
    private String avatarUrl;
    
    // Для загрузки через multipart
    public AvatarUploadRequest() {}
    
    public AvatarUploadRequest(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}