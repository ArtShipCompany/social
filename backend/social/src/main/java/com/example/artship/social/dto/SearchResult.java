package com.example.artship.social.dto;

import java.util.List;

public class SearchResult {
    private List<ArtDto> artsByTitle;    
    private List<ArtDto> artsByTags;       
    private List<UserDto> usersByUsername; 
    private long totalArtsByTitle;
    private long totalArtsByTags;
    private long totalUsers;
    private String searchType;              
    
    public SearchResult() {}
    
    // Геттеры и сеттеры
    public List<ArtDto> getArtsByTitle() { return artsByTitle; }
    public void setArtsByTitle(List<ArtDto> artsByTitle) { this.artsByTitle = artsByTitle; }
    
    public List<ArtDto> getArtsByTags() { return artsByTags; }
    public void setArtsByTags(List<ArtDto> artsByTags) { this.artsByTags = artsByTags; }
    
    public List<UserDto> getUsersByUsername() { return usersByUsername; }
    public void setUsersByUsername(List<UserDto> usersByUsername) { this.usersByUsername = usersByUsername; }
    
    public long getTotalArtsByTitle() { return totalArtsByTitle; }
    public void setTotalArtsByTitle(long totalArtsByTitle) { this.totalArtsByTitle = totalArtsByTitle; }
    
    public long getTotalArtsByTags() { return totalArtsByTags; }
    public void setTotalArtsByTags(long totalArtsByTags) { this.totalArtsByTags = totalArtsByTags; }
    
    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    
    public String getSearchType() { return searchType; }
    public void setSearchType(String searchType) { this.searchType = searchType; }
}