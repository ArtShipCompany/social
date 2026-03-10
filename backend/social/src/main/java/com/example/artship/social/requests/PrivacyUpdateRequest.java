package com.example.artship.social.requests;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на изменение приватности арта")
public class PrivacyUpdateRequest {
    
    @Schema(description = "Приватность арта (true - публичный, false - приватный)", 
            example = "true", required = true)
    private boolean isPublic;
    
    public boolean isPublic() {
        return isPublic;
    }
    
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}