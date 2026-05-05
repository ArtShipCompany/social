package com.example.artship.social.model.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.artship.social.model.enumclass.ReportStatus;
import com.example.artship.social.model.enumclass.ReportTargetType;

import java.time.LocalDateTime;

@Document(collection = "reports")
public class Report {
    
    @Id
    private String id;
    
    @Indexed
    private Long reporterId;        
    
    @Indexed
    private Long targetId;           
    
    @Indexed
    private ReportTargetType targetType; 
    
    private String reason;            
    private String description;       
    
    @Indexed
    private ReportStatus status = ReportStatus.PENDING; 
    
    private String resolvedBy;        // Кто обработал (username админа)
    private String resolutionNote;    // Заметка по обработке
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    
    private int priority = 0;         
    private boolean isAutoBanned = false; 
    
    private String artTitle;          
    private String artAuthorUsername; 
    
    private String commentText;       
    private String commentAuthorUsername; 
    
    public Report() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = ReportStatus.PENDING;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }
    
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    
    public ReportTargetType getTargetType() { return targetType; }
    public void setTargetType(ReportTargetType targetType) { this.targetType = targetType; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
    
    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
    
    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    
    public boolean isAutoBanned() { return isAutoBanned; }
    public void setAutoBanned(boolean autoBanned) { isAutoBanned = autoBanned; }
    
    public String getArtTitle() { return artTitle; }
    public void setArtTitle(String artTitle) { this.artTitle = artTitle; }
    
    public String getArtAuthorUsername() { return artAuthorUsername; }
    public void setArtAuthorUsername(String artAuthorUsername) { this.artAuthorUsername = artAuthorUsername; }
    
    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }
    
    public String getCommentAuthorUsername() { return commentAuthorUsername; }
    public void setCommentAuthorUsername(String commentAuthorUsername) { this.commentAuthorUsername = commentAuthorUsername; }
}