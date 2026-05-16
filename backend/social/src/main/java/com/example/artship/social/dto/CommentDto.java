package com.example.artship.social.dto;

import com.example.artship.social.model.Comment;
import java.time.LocalDateTime;
import java.util.List;

public class CommentDto {
    private Long id;
    private String text;
    private LocalDateTime createdAt;
    private UserDto author;
    private Long artId;
    private Long parentCommentId;
    private int replyCount;
    private List<CommentDto> replies;  
    private Long totalReplies;         
    
    private long likesCount;
    private boolean likedByCurrentUser;
    
    public CommentDto() {}
    
    public CommentDto(Comment comment, Long likesCount) {
        this.id = comment.getId();
        this.text = comment.getText();
        this.createdAt = comment.getCreatedAt();
        this.artId = comment.getArt() != null ? comment.getArt().getId() : null;
        
        if (comment.getParentComment() != null) {
            this.parentCommentId = comment.getParentComment().getId();
        }
        
        if (comment.getUser() != null) {
            this.author = new UserDto(comment.getUser());
        }
        
        this.likesCount = likesCount != null ? likesCount : 0;
        this.likedByCurrentUser = false;
    }
    
    public CommentDto(Comment comment, Long likesCount, boolean likedByCurrentUser) {
        this.id = comment.getId();
        this.text = comment.getText();
        this.createdAt = comment.getCreatedAt();
        this.artId = comment.getArt() != null ? comment.getArt().getId() : null;
        
        if (comment.getParentComment() != null) {
            this.parentCommentId = comment.getParentComment().getId();
        }
        
        if (comment.getUser() != null) {
            this.author = new UserDto(comment.getUser());
        }
        
        this.likesCount = likesCount != null ? likesCount : 0;
        this.likedByCurrentUser = likedByCurrentUser;
    }
    
    public CommentDto(Comment comment) {
        this.id = comment.getId();
        this.text = comment.getText();
        this.createdAt = comment.getCreatedAt();
        this.artId = comment.getArt() != null ? comment.getArt().getId() : null;
        
        if (comment.getParentComment() != null) {
            this.parentCommentId = comment.getParentComment().getId();
        }
        
        if (comment.getUser() != null) {
            this.author = new UserDto(comment.getUser());
        }
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public UserDto getAuthor() { return author; }
    public void setAuthor(UserDto author) { this.author = author; }
    
    public Long getArtId() { return artId; }
    public void setArtId(Long artId) { this.artId = artId; }
    
    public Long getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(Long parentCommentId) { this.parentCommentId = parentCommentId; }
    
    public int getReplyCount() { return replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }
    
    public List<CommentDto> getReplies() { return replies; }
    public void setReplies(List<CommentDto> replies) { this.replies = replies; }
    
    public Long getTotalReplies() { return totalReplies; }
    public void setTotalReplies(Long totalReplies) { this.totalReplies = totalReplies; }
    
    public long getLikesCount() { return likesCount; }
    public void setLikesCount(long likesCount) { this.likesCount = likesCount; }
    
    public boolean isLikedByCurrentUser() { return likedByCurrentUser; }
    public void setLikedByCurrentUser(boolean likedByCurrentUser) { this.likedByCurrentUser = likedByCurrentUser; }
}