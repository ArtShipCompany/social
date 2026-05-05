package com.example.artship.social.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReportRequest {
    
    @NotNull(message = "Target ID is required")
    private Long targetId;
    
    @NotNull(message = "Target type is required")
    private String targetType; 
    
    @NotBlank(message = "Reason is required")
    private String reason;
    
    private String description;
    
    // Getters and setters
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}