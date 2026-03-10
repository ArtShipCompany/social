package com.example.artship.social.requests;


public class AvatarUploadRequest {
    private String avatarUrl;

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