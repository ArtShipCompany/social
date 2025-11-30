package com.example.artship.social.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;

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
    }
    
    @PrePersist
    protected void onCreate() {
        savedAt = LocalDateTime.now();
    }
    

    public Collection getCollection() { return collection; }
    public Art getArt() { return art; }
    public LocalDateTime getSavedAt() { return savedAt; }
    

    public void setCollection(Collection collection) { this.collection = collection; }
    public void setArt(Art art) { this.art = art; }
    public void setSavedAt(LocalDateTime savedAt) { this.savedAt = savedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CollectionArt that)) return false;
        return collection != null && collection.equals(that.collection) && 
               art != null && art.equals(that.art);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(collection, art);
    }
    
    @Override
    public String toString() {
        return "CollectionArt{collection=" + (collection != null ? collection.getTitle() : "null") + 
               ", art=" + (art != null ? art.getId() : "null") + "}";
    }

    public class CollectionArtId implements java.io.Serializable {
        private Long collection;
        private Long art;
        
        public CollectionArtId() {}
        
        public CollectionArtId(Long collection, Long art) {
            this.collection = collection;
            this.art = art;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CollectionArtId that)) return false;
            return collection.equals(that.collection) && art.equals(that.art);
        }
        
        @Override
        public int hashCode() {
            return java.util.Objects.hash(collection, art);
        }
}
}


