package com.example.artship.social.requests;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Запрос на изменение приватности арта")
public class PrivacyUpdateRequest {
    
    @Schema(description = "Приватность арта (true - публичный, false - приватный)", 
            example = "true", required = true)
    private Boolean isPublicFlag;  
  
    public Boolean getIsPublicFlag() {
        return isPublicFlag;
    }
    
    public void setIsPublicFlag(Boolean isPublicFlag) {
        this.isPublicFlag = isPublicFlag;
    }
    
    @Override
    public String toString() {
        return "UpdateArtPrivacyRequest{isPublicFlag=" + isPublicFlag + "}";
    }
}