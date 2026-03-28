package com.example.artship.social.response;
import com.example.artship.social.model.SocialPlatform;
import java.time.LocalDateTime;

public class SocialLinkResponse {
    
    private Long id;  
    private SocialPlatform platform;
    private String url;
    private boolean visible;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public SocialLinkResponse() {}
    
    public SocialLinkResponse(Long id, SocialPlatform platform, String url, boolean visible, Integer displayOrder,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.platform = platform;
        this.url = url;
        this.visible = visible;
        this.displayOrder = displayOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public SocialPlatform getPlatform() {
        return platform;
    }
    
    public void setPlatform(SocialPlatform platform) {
        this.platform = platform;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }


    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}