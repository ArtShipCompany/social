package com.example.artship.social.requests;



import com.example.artship.social.model.SocialPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SocialLinkRequest {
    
    @NotNull(message = "Platform is required")
    private SocialPlatform platform;
    
    @NotBlank(message = "URL is required")
    @Size(max = 500, message = "URL must be less than 500 characters")
    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?$", 
             message = "Invalid URL format")
    private String url;
    
    private boolean visible = true;
    
    private Integer displayOrder = 0;
    
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
}