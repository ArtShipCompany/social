package com.example.artship.social.dto;

import java.time.LocalDateTime;

import com.example.artship.social.model.Art;


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

    public ArtDto(Art art) {
        this.id = art.getId();
        this.title = art.getTitle();
        this.description = art.getDescription();
        this.image = art.getImageUrl();
        this.projectDataUrl = art.getProjectDataUrl();
        this.isPublic = art.getIsPublic();
        this.createdAt = art.getCreatedAt();
        this.updatedAt = art.getUpdatedAt();
        this.author = art.getAuthor() != null ? new UserDto(art.getAuthor()) : null;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImage() { return image; }
    public String getProjectDataUrl() { return projectDataUrl; }
    public boolean isPublic() { return isPublic; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public UserDto getAuthor() { return author; }
}