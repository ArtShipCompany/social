package com.example.artship.social.dto;

import com.example.artship.social.model.CommentLikes;
import java.time.LocalDateTime;

public class CommentLikesDto {
    private Long userId;
    private String username;
    private String userAvatarUrl;
    private Long commentId;
    private LocalDateTime createdAt;
    
    public CommentLikesDto() {}
    
    public CommentLikesDto(CommentLikes like) {
        this.userId = like.getUser() != null ? like.getUser().getId() : null;
        this.username = like.getUser() != null ? like.getUser().getUsername() : null;
        this.userAvatarUrl = like.getUser() != null ? like.getUser().getAvatarUrl() : null;
        this.commentId = like.getComment() != null ? like.getComment().getId() : null;
        this.createdAt = like.getCreatedAt();
    }
    
    public CommentLikesDto(Long userId, String username, String userAvatarUrl, Long commentId, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.userAvatarUrl = userAvatarUrl;
        this.commentId = commentId;
        this.createdAt = createdAt;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }
    
    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }
    
    public Long getCommentId() {
        return commentId;
    }
    
    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}