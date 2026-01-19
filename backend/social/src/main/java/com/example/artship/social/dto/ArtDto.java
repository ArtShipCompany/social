package com.example.artship.social.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.example.artship.social.model.Art;
import com.example.artship.social.model.User;

public class ArtDto {
    private Long id;
    private String title;
    private String description;
    private String image;
    private String projectDataUrl;
    private boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt; 
    private UserDto author;
    private List<TagDto> tags;

    public ArtDto() {}

    public ArtDto(Art art) {
        this.id = art.getId();
        this.title = art.getTitle();
        this.description = art.getDescription();
        this.image = art.getImageUrl();
        this.projectDataUrl = art.getProjectDataUrl();
        this.isPublic = art.getIsPublic() != null ? art.getIsPublic() : true;
        this.createdAt = art.getCreatedAt();
        this.updatedAt = art.getUpdatedAt();

        if (art.getAuthor() != null) {
            this.author = new UserDto();
            this.author.setId(art.getAuthor().getId());
            this.author.setUsername(art.getAuthor().getUsername());
            this.author.setEmail(art.getAuthor().getEmail());
            this.author.setAvatarUrl(art.getAuthor().getAvatarUrl());
        }
        
        this.tags = null; 
    }


    public ArtDto(Art art, List<TagDto> tags) {
        this.id = art.getId();
        this.title = art.getTitle();
        this.description = art.getDescription();
        this.image = art.getImageUrl();
        this.projectDataUrl = art.getProjectDataUrl();
        this.isPublic = art.getIsPublic() != null ? art.getIsPublic() : true;
        this.createdAt = art.getCreatedAt();
        this.updatedAt = art.getUpdatedAt();
        
        if (art.getAuthor() != null) {
            this.author = new UserDto();
            this.author.setId(art.getAuthor().getId());
            this.author.setUsername(art.getAuthor().getUsername());
            this.author.setEmail(art.getAuthor().getEmail());
            this.author.setAvatarUrl(art.getAuthor().getAvatarUrl());
        }
        
        this.tags = tags;
        }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    
    public String getProjectDataUrl() { return projectDataUrl; }
    public void setProjectDataUrl(String projectDataUrl) { this.projectDataUrl = projectDataUrl; }
    
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public UserDto getAuthor() { return author; }
    public void setAuthor(UserDto author) { this.author = author; }
    
    public List<TagDto> getTags() { return tags; }
    public void setTags(List<TagDto> tags) { this.tags = tags; }
}