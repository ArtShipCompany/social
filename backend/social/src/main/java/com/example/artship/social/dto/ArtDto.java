package com.example.artship.social.dto;

import java.time.LocalDateTime;

import com.example.artship.social.model.Art;
import com.example.artship.social.model.Tag;

import java.util.List;
import java.util.stream.Collectors;

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

    // Конструктор из Entity БЕЗ тегов
    public ArtDto(Art art) {
        this.id = art.getId();
        this.title = art.getTitle();
        this.description = art.getDescription();
        this.image = art.getImageUrl();
        this.projectDataUrl = art.getProjectDataUrl();
        this.isPublic = art.getIsPublic() != null ? art.getIsPublic() : true;
        this.createdAt = art.getCreatedAt();
        this.updatedAt = art.getUpdatedAt();
        this.author = art.getAuthor() != null ? new UserDto(art.getAuthor()) : null;
        this.tags = null;
    }

    public ArtDto(Art art, List<Tag> tags) {
        this.id = art.getId();
        this.title = art.getTitle();
        this.description = art.getDescription();
        this.image = art.getImageUrl();
        this.projectDataUrl = art.getProjectDataUrl();
        this.isPublic = art.getIsPublic() != null ? art.getIsPublic() : true;
        this.createdAt = art.getCreatedAt();
        this.updatedAt = art.getUpdatedAt();
        this.author = art.getAuthor() != null ? new UserDto(art.getAuthor()) : null;
        this.tags = tags != null ? 
            tags.stream().map(TagDto::new).collect(Collectors.toList()) : 
            null;
    }

    // Конструктор копирования
    public ArtDto(ArtDto art) {
        this.id = art.getId();
        this.title = art.getTitle();
        this.description = art.getDescription();
        this.image = art.getImage();
        this.projectDataUrl = art.getProjectDataUrl();
        this.isPublic = art.isPublic();
        this.createdAt = art.getCreatedAt();
        this.updatedAt = art.getUpdatedAt();
        this.author = art.getAuthor() != null ? new UserDto(art.getAuthor()) : null;
        this.tags = art.getTags() != null ?
            art.getTags().stream().map(TagDto::new).collect(Collectors.toList()) :
            null;
    }

    // Геттеры
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImage() { return image; }
    public String getProjectDataUrl() { return projectDataUrl; }
    public boolean isPublic() { return isPublic; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public UserDto getAuthor() { return author; }
    public List<TagDto> getTags() { return tags; }

    // Сеттеры
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setImage(String image) { this.image = image; }
    public void setProjectDataUrl(String projectDataUrl) { this.projectDataUrl = projectDataUrl; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setAuthor(UserDto author) { this.author = author; }
    public void setTags(List<TagDto> tags) { this.tags = tags; }
}