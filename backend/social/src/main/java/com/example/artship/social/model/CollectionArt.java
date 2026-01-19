package com.example.artship.social.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "collection_arts")
@IdClass(CollectionArt.CollectionArtId.class)  
public class CollectionArt {
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "art_id", nullable = false)
    private Art art;
    
    @Column(name = "saved_at")
    private LocalDateTime savedAt;
    
    
    public CollectionArt() {}
    
    public CollectionArt(Collection collection, Art art) {
        this.collection = collection;
        this.art = art;
        this.savedAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        if (savedAt == null) {
            savedAt = LocalDateTime.now();
        }
    }
    
    // Геттеры и сеттеры
    public Collection getCollection() { return collection; }
    public void setCollection(Collection collection) { this.collection = collection; }
    
    public Art getArt() { return art; }
    public void setArt(Art art) { this.art = art; }
    
    public LocalDateTime getSavedAt() { return savedAt; }
    public void setSavedAt(LocalDateTime savedAt) { this.savedAt = savedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CollectionArt that)) return false;
        return Objects.equals(collection.getId(), that.collection.getId()) && 
               Objects.equals(art.getId(), that.art.getId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(collection.getId(), art.getId());
    }
    
    @Override
    public String toString() {
        return "CollectionArt{" +
               "collection=" + (collection != null ? collection.getTitle() : "null") + 
               ", art=" + (art != null ? art.getId() : "null") + 
               ", savedAt=" + savedAt + 
               '}';
    }


    public static class CollectionArtId implements java.io.Serializable {
        private Long collection;  
        private Long art;         
        

        public CollectionArtId() {}
        
        public CollectionArtId(Long collection, Long art) {
            this.collection = collection;
            this.art = art;
        }

        public Long getCollection() { return collection; }
        public void setCollection(Long collection) { this.collection = collection; }
        
        public Long getArt() { return art; }
        public void setArt(Long art) { this.art = art; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CollectionArtId)) return false;
            CollectionArtId that = (CollectionArtId) o;
            return Objects.equals(collection, that.collection) && 
                   Objects.equals(art, that.art);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(collection, art);
        }
    }
}