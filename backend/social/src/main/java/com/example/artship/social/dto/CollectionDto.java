package com.example.artship.social.dto;

import com.example.artship.social.model.Collection;
import java.time.LocalDateTime;
import java.util.List;

public class CollectionDto {
    private Long id;
    private String title;
    private String description;
    private Boolean isPublic;
    private String coverImageUrl;
    private LocalDateTime createdAt;
    private Long userId;
    private String username;
    private Integer artCount;
    private List<ArtDto> arts;

    public CollectionDto() {}

    public CollectionDto(Collection collection) {
        this.id = collection.getId();
        this.title = collection.getTitle();
        this.description = collection.getDescription();
        this.isPublic = collection.getIsPublic();
        this.coverImageUrl = collection.getCoverImageUrl();
        this.createdAt = collection.getCreatedAt();
        this.userId = collection.getUser() != null ? collection.getUser().getId() : null;
        this.username = collection.getUser() != null ? collection.getUser().getUsername() : null;
        this.artCount = collection.getCollectionArts() != null ? collection.getCollectionArts().size() : 0;
        this.arts = null; // Загружаются отдельно
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getArtCount() { return artCount; }
    public void setArtCount(Integer artCount) { this.artCount = artCount; }

    public List<ArtDto> getArts() { return arts; }
    public void setArts(List<ArtDto> arts) { this.arts = arts; }
}