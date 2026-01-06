package com.example.artship.social.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "follows")
@IdClass(Follow.FollowId.class)  // Указываем класс для составного ключа
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
        this.createdAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    // Геттеры и сеттеры
    public User getFollower() { return follower; }
    public void setFollower(User follower) { this.follower = follower; }
    
    public User getFollowing() { return following; }
    public void setFollowing(User following) { this.following = following; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Follow follow)) return false;
        return Objects.equals(follower.getId(), follow.follower.getId()) && 
               Objects.equals(following.getId(), follow.following.getId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(follower.getId(), following.getId());
    }
    
    @Override
    public String toString() {
        return "Follow{" +
               "follower=" + (follower != null ? follower.getUsername() : "null") + 
               ", following=" + (following != null ? following.getUsername() : "null") + 
               ", createdAt=" + createdAt + 
               '}';
    }

    // ВАЖНО: Класс должен быть static и public
    public static class FollowId implements java.io.Serializable {
        private Long follower;  // Должен совпадать с именем поля в Follow
        private Long following; // Должен совпадать с именем поля в Follow
        
        public FollowId() {}
        
        public FollowId(Long follower, Long following) {
            this.follower = follower;
            this.following = following;
        }
        
        // Геттеры и сеттеры
        public Long getFollower() { return follower; }
        public void setFollower(Long follower) { this.follower = follower; }
        
        public Long getFollowing() { return following; }
        public void setFollowing(Long following) { this.following = following; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FollowId)) return false;
            FollowId followId = (FollowId) o;
            return Objects.equals(follower, followId.follower) && 
                   Objects.equals(following, followId.following);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(follower, following);
        }
    }
}