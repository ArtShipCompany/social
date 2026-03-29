package com.example.artship.social.dto;

import java.util.List;

public class UnifiedSearchResult {
    private List<ArtDto> arts;
    private List<UserDto> users;
    private long totalArts;
    private long totalUsers;
    
    public UnifiedSearchResult() {}
    
    public UnifiedSearchResult(List<ArtDto> arts, List<UserDto> users) {
        this.arts = arts;
        this.users = users;
        this.totalArts = arts != null ? arts.size() : 0;
        this.totalUsers = users != null ? users.size() : 0;
    }
    
    public UnifiedSearchResult(List<ArtDto> arts, List<UserDto> users, long totalArts, long totalUsers) {
        this.arts = arts;
        this.users = users;
        this.totalArts = totalArts;
        this.totalUsers = totalUsers;
    }
    
    public List<ArtDto> getArts() {
        return arts;
    }
    
    public void setArts(List<ArtDto> arts) {
        this.arts = arts;
    }
    
    public List<UserDto> getUsers() {
        return users;
    }
    
    public void setUsers(List<UserDto> users) {
        this.users = users;
    }
    
    public long getTotalArts() {
        return totalArts;
    }
    
    public void setTotalArts(long totalArts) {
        this.totalArts = totalArts;
    }
    
    public long getTotalUsers() {
        return totalUsers;
    }
    
    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }
}