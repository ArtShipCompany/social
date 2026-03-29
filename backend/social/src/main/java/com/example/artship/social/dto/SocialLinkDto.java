package com.example.artship.social.dto;

import com.example.artship.social.model.SocialPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SocialLinkDto {
    
    private Long id;
    
    @NotNull(message = "Platform is required")
    private SocialPlatform platform;
    
    @NotBlank(message = "URL is required")
    @Size(max = 500, message = "URL must be less than 500 characters")
    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?$", 
             message = "Invalid URL format")
    private String url;
    
    @Size(max = 100, message = "Username must be less than 100 characters")
    private String username;
    
    private boolean visible = true;
    
    private Integer displayOrder = 0;
    
    public SocialLinkDto() {}
    
    public SocialLinkDto(Long id, SocialPlatform platform, String url, 
                         boolean visible, Integer displayOrder) {
        this.id = id;
        this.platform = platform;
        this.url = url;
        this.visible = visible;
        this.displayOrder = displayOrder;
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
}



