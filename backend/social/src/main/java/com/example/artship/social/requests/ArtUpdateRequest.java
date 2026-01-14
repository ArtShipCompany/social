package com.example.artship.social.requests;



public class ArtUpdateRequest {
    private String title;
    private String description;
    private String projectDataUrl;
    private Boolean isPublic;
    
    // Конструкторы
    public ArtUpdateRequest() {}
    
    // Геттеры и сеттеры
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getProjectDataUrl() {
        return projectDataUrl;
    }
    
    public void setProjectDataUrl(String projectDataUrl) {
        this.projectDataUrl = projectDataUrl;
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    @Override
    public String toString() {
        return "ArtUpdateRequest{" +
               "title='" + title + '\'' +
               ", description='" + description + '\'' +
               ", projectDataUrl='" + projectDataUrl + '\'' +
               ", isPublic=" + isPublic +
               '}';
    }
}