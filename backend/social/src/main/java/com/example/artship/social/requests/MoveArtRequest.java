package com.example.artship.social.requests;

public class MoveArtRequest {
    private Long fromCollectionId;
        private Long toCollectionId;
        
        public Long getFromCollectionId() { return fromCollectionId; }
        public void setFromCollectionId(Long fromCollectionId) { this.fromCollectionId = fromCollectionId; }
        
        public Long getToCollectionId() { return toCollectionId; }
        public void setToCollectionId(Long toCollectionId) { this.toCollectionId = toCollectionId; }
    
    
}
