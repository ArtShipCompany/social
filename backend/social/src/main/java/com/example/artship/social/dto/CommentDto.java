package com.example.artship.social.dto;

import com.example.artship.social.model.Comment;
import java.time.LocalDateTime;
import java.util.List;

public class CommentDto {
    private Long id;
    private String text;
    private LocalDateTime createdAt;
    private Long artId;
    private Long userId;
    private String username;
    private String userAvatarUrl;
    private Long parentCommentId;
    private List<CommentDto> replies;

    public CommentDto() {}

    public CommentDto(Comment comment) {
        this.id = comment.getId();
        this.text = comment.getText();
        this.createdAt = comment.getCreatedAt();
        this.artId = comment.getArt() != null ? comment.getArt().getId() : null;
        this.userId = comment.getUser() != null ? comment.getUser().getId() : null;
        this.username = comment.getUser() != null ? comment.getUser().getUsername() : null;
        this.userAvatarUrl = comment.getUser() != null ? comment.getUser().getAvatarUrl() : null;
        this.parentCommentId = comment.getParentComment() != null ? comment.getParentComment().getId() : null;
        this.replies = null; // Загружаются отдельно
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getArtId() { return artId; }
    public void setArtId(Long artId) { this.artId = artId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserAvatarUrl() { return userAvatarUrl; }
    public void setUserAvatarUrl(String userAvatarUrl) { this.userAvatarUrl = userAvatarUrl; }

    public Long getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(Long parentCommentId) { this.parentCommentId = parentCommentId; }

    public List<CommentDto> getReplies() { return replies; }
    public void setReplies(List<CommentDto> replies) { this.replies = replies; }
}