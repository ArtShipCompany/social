package com.example.artship.social.model;



import jakarta.persistence.*;

@Entity
@Table(name = "art_tags")
@IdClass(ArtTagId.class)
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
        if (!(o instanceof ArtTag artTag)) return false;
        return art != null && art.equals(artTag.art) && 
               tag != null && tag.equals(artTag.tag);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(art, tag);
    }
    
    @Override
    public String toString() {
        return "ArtTag{art=" + (art != null ? art.getId() : "null") + 
               ", tag=" + (tag != null ? tag.getName() : "null") + "}";
    }
}


class ArtTagId implements java.io.Serializable {
    private Long art;
    private Long tag;
    
    public ArtTagId() {}
    
    public ArtTagId(Long art, Long tag) {
        this.art = art;
        this.tag = tag;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArtTagId that)) return false;
        return art.equals(that.art) && tag.equals(that.tag);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(art, tag);
    }
}