package com.example.artship.social.dto;

import com.example.artship.social.model.Like;
import java.time.LocalDateTime;

public class LikeDto {
    private Long userId;
    private String username;
    private Long artId;
    private String artTitle;
    private LocalDateTime createdAt;

    public LikeDto() {}

    public LikeDto(Like like) {
        this.userId = like.getUser() != null ? like.getUser().getId() : null;
        this.username = like.getUser() != null ? like.getUser().getUsername() : null;
        this.artId = like.getArt() != null ? like.getArt().getId() : null;
        this.artTitle = like.getArt() != null ? like.getArt().getTitle() : null;
        this.createdAt = like.getCreatedAt();
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getArtId() { return artId; }
    public void setArtId(Long artId) { this.artId = artId; }

    public String getArtTitle() { return artTitle; }
    public void setArtTitle(String artTitle) { this.artTitle = artTitle; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}