package com.example.artship.social.dto;

import com.example.artship.social.model.Follow;
import java.time.LocalDateTime;

public class FollowDto {
    private Long followerId;
    private Long followingId;
    private String followerUsername;
    private String followingUsername;
    private LocalDateTime createdAt;
    
    public FollowDto() {}
    
    public FollowDto(Follow follow) {
        this.followerId = follow.getFollower().getId();
        this.followingId = follow.getFollowing().getId();
        this.followerUsername = follow.getFollower().getUsername();
        this.followingUsername = follow.getFollowing().getUsername();
        this.createdAt = follow.getCreatedAt();
    }
    

    public Long getFollowerId() { return followerId; }
    public void setFollowerId(Long followerId) { this.followerId = followerId; }
    
    public Long getFollowingId() { return followingId; }
    public void setFollowingId(Long followingId) { this.followingId = followingId; }
    
    public String getFollowerUsername() { return followerUsername; }
    public void setFollowerUsername(String followerUsername) { this.followerUsername = followerUsername; }
    
    public String getFollowingUsername() { return followingUsername; }
    public void setFollowingUsername(String followingUsername) { this.followingUsername = followingUsername; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}