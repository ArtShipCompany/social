package com.example.artship.social.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateArtRequest {
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("description") 
    private String description;
    
    @JsonProperty("image")
    private String image;
    
    @JsonProperty("projectDataUrl")
    private String projectDataUrl;
    
    @JsonProperty("isPublic")
    private Boolean isPublic = true;
    
    @JsonProperty("authorId")
    private Long authorId;

    // Геттеры и сеттеры
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getProjectDataUrl() { return projectDataUrl; }
    public void setProjectDataUrl(String projectDataUrl) { this.projectDataUrl = projectDataUrl; }

    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    @Override
    public String toString() {
        return "CreateArtRequest{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", projectDataUrl='" + projectDataUrl + '\'' +
                ", isPublic=" + isPublic +
                ", authorId=" + authorId +
                '}';
    }
}