package com.example.artship.social.requests;

public  class CommentRequest {
        private String text;
        private Long artId;
        private Long userId;
        private Long parentCommentId;
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public Long getArtId() { return artId; }
        public void setArtId(Long artId) { this.artId = artId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getParentCommentId() { return parentCommentId; }
        public void setParentCommentId(Long parentCommentId) { this.parentCommentId = parentCommentId; }
    }