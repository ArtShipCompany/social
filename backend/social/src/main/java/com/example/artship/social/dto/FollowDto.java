package com.example.artship.social.dto;

import com.example.artship.social.model.Follow;
import java.time.LocalDateTime;

public class FollowDto {
    private Long followerId;
    private String followerUsername;
    private Long followingId;
    private String followingUsername;
    private LocalDateTime createdAt;

    public FollowDto() {}

    public FollowDto(Follow follow) {
        this.followerId = follow.getFollower() != null ? follow.getFollower().getId() : null;
        this.followerUsername = follow.getFollower() != null ? follow.getFollower().getUsername() : null;
        this.followingId = follow.getFollowing() != null ? follow.getFollowing().getId() : null;
        this.followingUsername = follow.getFollowing() != null ? follow.getFollowing().getUsername() : null;
        this.createdAt = follow.getCreatedAt();
    }


    public Long getFollowerId() { return followerId; }
    public void setFollowerId(Long followerId) { this.followerId = followerId; }

    public String getFollowerUsername() { return followerUsername; }
    public void setFollowerUsername(String followerUsername) { this.followerUsername = followerUsername; }

    public Long getFollowingId() { return followingId; }
    public void setFollowingId(Long followingId) { this.followingId = followingId; }

    public String getFollowingUsername() { return followingUsername; }
    public void setFollowingUsername(String followingUsername) { this.followingUsername = followingUsername; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}