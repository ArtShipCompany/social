package com.example.artship.social.dto;

import com.example.artship.social.model.ArtTag;
import java.time.LocalDateTime;

public class ArtTagDto {
    private Long artId;
    private String artTitle;
    private Long tagId;
    private String tagName;
    private LocalDateTime createdAt;

    public ArtTagDto() {}

    public ArtTagDto(ArtTag artTag) {
        this.artId = artTag.getArt() != null ? artTag.getArt().getId() : null;
        this.artTitle = artTag.getArt() != null ? artTag.getArt().getTitle() : null;
        this.tagId = artTag.getTag() != null ? artTag.getTag().getId() : null;
        this.tagName = artTag.getTag() != null ? artTag.getTag().getName() : null;
        // Если нужно добавить дату создания связи, можно добавить поле в ArtTag entity
    }

    public ArtTagDto(Long artId, String artTitle, Long tagId, String tagName) {
        this.artId = artId;
        this.artTitle = artTitle;
        this.tagId = tagId;
        this.tagName = tagName;
    }

    // Геттеры и сеттеры
    public Long getArtId() { return artId; }
    public void setArtId(Long artId) { this.artId = artId; }

    public String getArtTitle() { return artTitle; }
    public void setArtTitle(String artTitle) { this.artTitle = artTitle; }

    public Long getTagId() { return tagId; }
    public void setTagId(Long tagId) { this.tagId = tagId; }

    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "ArtTagDto{" +
                "artId=" + artId +
                ", artTitle='" + artTitle + '\'' +
                ", tagId=" + tagId +
                ", tagName='" + tagName + '\'' +
                '}';
    }
}