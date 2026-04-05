package com.example.artship.social.dto;

import com.example.artship.social.model.Follow;
import java.time.LocalDateTime;

public class FollowDto {
    private UserFollowInfoDto follower;
    private UserFollowInfoDto following;
    private LocalDateTime createdAt;
    
    public FollowDto() {}
    
    public FollowDto(Follow follow) {
        this.follower = new UserFollowInfoDto(follow.getFollower());
        this.following = new UserFollowInfoDto(follow.getFollowing());
        this.createdAt = follow.getCreatedAt();
    }
    
    public UserFollowInfoDto getFollower() { 
        return follower; 
    }
    
    public void setFollower(UserFollowInfoDto follower) { 
        this.follower = follower; 
    }
    
    public UserFollowInfoDto getFollowing() { 
        return following; 
    }
    
    public void setFollowing(UserFollowInfoDto following) { 
        this.following = following; 
    }
    
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }
}