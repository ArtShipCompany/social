package com.example.artship.social.requests;

import org.springframework.web.multipart.MultipartFile;

public class CollectionUpdateRequest {
    private String title;
    private String description;
    private Boolean isPublic;
    private String coverImageUrl;
    private MultipartFile coverImageFile;  
        
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
        
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
        
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
        
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    
    public MultipartFile getCoverImageFile() { return coverImageFile; }
    public void setCoverImageFile(MultipartFile coverImageFile) { this.coverImageFile = coverImageFile; }
}