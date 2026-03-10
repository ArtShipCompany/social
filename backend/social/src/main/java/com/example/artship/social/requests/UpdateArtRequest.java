package com.example.artship.social.requests;

import org.springframework.web.multipart.MultipartFile;

public class UpdateArtRequest {
        private String title;
        private String description;
        private String projectDataUrl;
        private MultipartFile imageFile;
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getProjectDataUrl() {
            return projectDataUrl;
        }
        
        public void setProjectDataUrl(String projectDataUrl) {
            this.projectDataUrl = projectDataUrl;
        }
        

        
        public MultipartFile getImageFile() {
            return imageFile;
        }
        
        public void setImageFile(MultipartFile imageFile) {
            this.imageFile = imageFile;
        }
    }
