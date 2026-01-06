package com.example.artship.social.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "art_tags")
@IdClass(ArtTag.ArtTagId.class)
public class ArtTag {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "art_id", nullable = false)
    private Art art;
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
    
    public ArtTag() {}
    
    public ArtTag(Art art, Tag tag) {
        this.art = art;
        this.tag = tag;
    }
    
    public Art getArt() { return art; }
    public Tag getTag() { return tag; }
    
    public void setArt(Art art) { this.art = art; }
    public void setTag(Tag tag) { this.tag = tag; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArtTag)) return false;
        ArtTag artTag = (ArtTag) o;
        return Objects.equals(art.getId(), artTag.art.getId()) && 
               Objects.equals(tag.getId(), artTag.tag.getId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(art.getId(), tag.getId());
    }
    
    @Override
    public String toString() {
        return "ArtTag{art=" + (art != null ? art.getId() : "null") + 
               ", tag=" + (tag != null ? tag.getId() : "null") + "}";
    }

    // ВАЖНО: Класс должен быть static!
    public static class ArtTagId implements Serializable {
        private Long art;
        private Long tag;
        
        public ArtTagId() {}
        
        public ArtTagId(Long art, Long tag) {
            this.art = art;
            this.tag = tag;
        }
        
       
        public Long getArt() { return art; }
        public void setArt(Long art) { this.art = art; }
        
        public Long getTag() { return tag; }
        public void setTag(Long tag) { this.tag = tag; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ArtTagId)) return false;
            ArtTagId that = (ArtTagId) o;
            return Objects.equals(art, that.art) && Objects.equals(tag, that.tag);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(art, tag);
        }
    }
}