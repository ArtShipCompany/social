package com.example.artship.social.dto;

public class RoleStatistics {
    private long totalUsers;
    private long adminCount;
    private long moderatorCount;
    private long userCount;
    
    public RoleStatistics() {}
    
    public RoleStatistics(long totalUsers, long adminCount, long moderatorCount, long userCount) {
        this.totalUsers = totalUsers;
        this.adminCount = adminCount;
        this.moderatorCount = moderatorCount;
        this.userCount = userCount;
    }
    
    public long getTotalUsers() {
        return totalUsers;
    }
    
    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }
    
    public long getAdminCount() {
        return adminCount;
    }
    
    public void setAdminCount(long adminCount) {
        this.adminCount = adminCount;
    }
    
    public long getModeratorCount() {
        return moderatorCount;
    }
    
    public void setModeratorCount(long moderatorCount) {
        this.moderatorCount = moderatorCount;
    }
    
    public long getUserCount() {
        return userCount;
    }
    
    public void setUserCount(long userCount) {
        this.userCount = userCount;
    }
}