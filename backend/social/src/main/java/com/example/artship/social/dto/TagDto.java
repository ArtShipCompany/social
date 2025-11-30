package com.example.artship.social.dto;


import com.example.artship.social.model.Tag;
import java.util.List;
import java.util.stream.Collectors;

public class TagDto {
    private Long id;
    private String name;
    private int artCount;

    public TagDto() {}


    public TagDto(Tag tag) {
        this.id = tag.getId();
        this.name = tag.getName();
        this.artCount = tag.getArts() != null ? tag.getArts().size() : 0;
    }

    public TagDto(TagDto other) {
        this.id = other.id;
        this.name = other.name;
        this.artCount = other.artCount;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getArtCount() { return artCount; }
    public void setArtCount(int artCount) { this.artCount = artCount; }

    public static TagDto fromEntity(Tag tag) {
        return new TagDto(tag);
    }

    public static List<TagDto> fromEntities(List<Tag> tags) {
        return tags.stream()
                .map(TagDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "TagDto{id=" + id + ", name='" + name + "', artCount=" + artCount + "}";
    }
}