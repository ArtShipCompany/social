package com.example.artship.social.model;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.*;

@Entity
@Table(name = "comment_likes")
@IdClass(CommentLikes.CommentLikeId.class)
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
        this.createdAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Comment getComment() { return comment; }
    public void setComment(Comment comment) { this.comment = comment; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommentLikes like)) return false;
        return Objects.equals(user, like.user) && Objects.equals(comment, like.comment);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(user, comment);
    }
    
    @Override
    public String toString() {
        return "CommentLike{user=" + (user != null ? user.getUsername() : "null") + 
               ", comment=" + (comment != null ? comment.getId() : "null") + "}";
    }
    
    public static class CommentLikeId implements Serializable {
        private Long user;
        private Long comment;
        
        public CommentLikeId() {}
        
        public CommentLikeId(Long user, Long comment) {
            this.user = user;
            this.comment = comment;
        }
        
        public Long getUser() { return user; }
        public void setUser(Long user) { this.user = user; }
        
        public Long getComment() { return comment; }
        public void setComment(Long comment) { this.comment = comment; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CommentLikeId that = (CommentLikeId) o;
            return Objects.equals(user, that.user) && Objects.equals(comment, that.comment);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(user, comment);
        }
    }
}