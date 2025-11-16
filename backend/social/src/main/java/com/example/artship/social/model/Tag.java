package com.example.artship.social.model;


import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String name;
    
    @ManyToMany(mappedBy = "tags")
    private List<Art> arts = new ArrayList<>();
    
    public Tag() {}
    
    public Tag(String name) {
        this.name = name;
    }
    

    public Long getId() { return id; }
    public String getName() { return name; }
    public List<Art> getArts() { return arts; }
    

    public void setName(String name) { this.name = name; }
    public void setArts(List<Art> arts) { this.arts = arts; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag tag)) return false;
        return id != null && id.equals(tag.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Tag{id=" + id + ", name='" + name + "'}";
    }
}
