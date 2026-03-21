package com.example.artship.social.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "User profile update request")
public class UserUpdateRequest {

    @Schema(description = "Username (unique identifier)", 
            maxLength = 50)
    @Size(max = 50, message = "Username cannot exceed 50 characters")
    private String username;
    
    @Schema(description = "Display name shown to other users", 
            maxLength = 100)
    @Size(max = 100, message = "Display name cannot exceed 100 characters")
    private String displayName;
    
    @Schema(description = "User biography or description", 
            maxLength = 500)
    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;
    
    @Schema(description = "Avatar URL (use when not uploading file)")
    private String avatarUrl;
    
    @Schema(description = "Profile visibility (true = public, false = private)", 
            example = "true")
    private Boolean isPublic;
    
    public UserUpdateRequest() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
}