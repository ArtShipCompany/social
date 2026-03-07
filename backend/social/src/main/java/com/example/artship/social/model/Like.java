package com.example.artship.social.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "likes")
@IdClass(Like.LikeId.class)
public class Like {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "art_id", nullable = false)
    private Art art;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public Like() {}
    
    public Like(User user, Art art) {
        this.user = user;
        this.art = art;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    
    public User getUser() { return user; }
    public Art getArt() { return art; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public void setUser(User user) { this.user = user; }
    public void setArt(Art art) { this.art = art; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Like like)) return false;
        return user != null && user.equals(like.user) && art != null && art.equals(like.art);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(user, art);
    }
    
    @Override
    public String toString() {
        return "Like{user=" + (user != null ? user.getUsername() : "null") + 
               ", art=" + (art != null ? art.getId() : "null") + "}";
    }

    public static class LikeId implements java.io.Serializable {
        private Long user;
        private Long art;
        
        public LikeId() {}
        
        public LikeId(Long user, Long art) {
            this.user = user;
            this.art = art;
        }
        
        public Long getUser() { return user; }
        public void setUser(Long user) { this.user = user; }
        
        public Long getArt() { return art; }
        public void setArt(Long art) { this.art = art; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LikeId likeId)) return false;
            return java.util.Objects.equals(user, likeId.user) && 
                   java.util.Objects.equals(art, likeId.art);
        }
        
        @Override
        public int hashCode() {
            return java.util.Objects.hash(user, art);
        }
    }
}

