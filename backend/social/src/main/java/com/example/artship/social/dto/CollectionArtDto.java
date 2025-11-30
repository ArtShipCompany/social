package com.example.artship.social.dto;

import com.example.artship.social.model.CollectionArt;
import java.time.LocalDateTime;

public class CollectionArtDto {
    private Long collectionId;
    private String collectionTitle;
    private Long artId;
    private String artTitle;
    private String artImage;
    private LocalDateTime savedAt;

    public CollectionArtDto() {}

    public CollectionArtDto(CollectionArt collectionArt) {
        this.collectionId = collectionArt.getCollection() != null ? collectionArt.getCollection().getId() : null;
        this.collectionTitle = collectionArt.getCollection() != null ? collectionArt.getCollection().getTitle() : null;
        this.artId = collectionArt.getArt() != null ? collectionArt.getArt().getId() : null;
        this.artTitle = collectionArt.getArt() != null ? collectionArt.getArt().getTitle() : null;
        this.artImage = collectionArt.getArt() != null ? collectionArt.getArt().getImageUrl() : null;
        this.savedAt = collectionArt.getSavedAt();
    }
    public Long getCollectionId() { return collectionId; }
    public void setCollectionId(Long collectionId) { this.collectionId = collectionId; }

    public String getCollectionTitle() { return collectionTitle; }
    public void setCollectionTitle(String collectionTitle) { this.collectionTitle = collectionTitle; }

    public Long getArtId() { return artId; }
    public void setArtId(Long artId) { this.artId = artId; }

    public String getArtTitle() { return artTitle; }
    public void setArtTitle(String artTitle) { this.artTitle = artTitle; }

    public String getArtImage() { return artImage; }
    public void setArtImage(String artImage) { this.artImage = artImage; }

    public LocalDateTime getSavedAt() { return savedAt; }
    public void setSavedAt(LocalDateTime savedAt) { this.savedAt = savedAt; }
}