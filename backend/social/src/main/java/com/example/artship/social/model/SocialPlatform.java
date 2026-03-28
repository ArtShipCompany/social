package com.example.artship.social.model;

public enum SocialPlatform {
    TELEGRAM("Telegram", "https://t.me/"),
    VKONTAKTE("VKontakte", "https://vk.com/"),
    YOUTUBE("YouTube", "https://youtube.com/"),
    TWITTER("Twitter", "https://twitter.com/"),
    TIKTOK("TikTok", "https://tiktok.com/@");
    
    private final String displayName;
    private final String baseUrl;
    
    SocialPlatform(String displayName, String baseUrl) {
        this.displayName = displayName;
        this.baseUrl = baseUrl;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public String formatUrl(String username) {
        return baseUrl + username;
    }
}