package com.example.artship.social.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "follows")
@IdClass(FollowId.class)
public class Follow {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public Follow() {}
    
    public Follow(User follower, User following) {
        this.follower = follower;
        this.following = following;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    

    public User getFollower() { return follower; }
    public User getFollowing() { return following; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    

    public void setFollower(User follower) { this.follower = follower; }
    public void setFollowing(User following) { this.following = following; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Follow follow)) return false;
        return follower != null && follower.equals(follow.follower) && 
               following != null && following.equals(follow.following);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(follower, following);
    }
    
    @Override
    public String toString() {
        return "Follow{follower=" + (follower != null ? follower.getUsername() : "null") + 
               ", following=" + (following != null ? following.getUsername() : "null") + "}";
    }
}


class FollowId implements java.io.Serializable {
    private Long follower;
    private Long following;
    
    public FollowId() {}
    
    public FollowId(Long follower, Long following) {
        this.follower = follower;
        this.following = following;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FollowId followId)) return false;
        return follower.equals(followId.follower) && following.equals(followId.following);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(follower, following);
    }
}