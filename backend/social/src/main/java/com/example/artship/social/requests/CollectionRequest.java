package com.example.artship.social.requests;

public class CollectionRequest {
    private String title;
    private String description;
    private Boolean isPublic;
    private String coverImageUrl;
    private Long userId;
        
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
        
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
        
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
        
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
        
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}