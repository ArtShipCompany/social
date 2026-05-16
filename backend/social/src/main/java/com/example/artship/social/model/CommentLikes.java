package com.example.artship.social.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "comment_likes")
public class CommentLikes {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public CommentLikes() {}
    
    public CommentLikes(User user, Comment comment) {
        this.user = user;
        this.comment = comment;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    
    public User getUser() { return user; }
    public Comment getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public void setUser(User user) { this.user = user; }
    public void setSomment(Comment comment) { this.comment = comment; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommentLikes like)) return false;
        return user != null && user.equals(like.user) && comment != null && comment.equals(like.comment);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(user, comment);
    }
    
    @Override
    public String toString() {
        return "Like{user=" + (user != null ? user.getUsername() : "null") + 
               ", comment =" + (comment != null ? comment.getId() : "null") + "}";
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
