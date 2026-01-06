package com.example.artship.social.service;

import com.example.artship.social.dto.TagDto;
import com.example.artship.social.model.Tag;
import com.example.artship.social.repository.ArtTagRepository;
import com.example.artship.social.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TagService {
    
    private final TagRepository tagRepository;

    private final ArtTagRepository artTagRepository;
    
    public TagService(TagRepository tagRepository, ArtTagRepository artTagRepository) {
    this.tagRepository = tagRepository;
    this.artTagRepository = artTagRepository;
}
    
    public Tag createTag(String name) {
        Optional<Tag> existingTag = tagRepository.findByName(name);
        if (existingTag.isPresent()) {
            return existingTag.get();
        }
        
        Tag tag = new Tag(name);
        return tagRepository.save(tag);
    }
    

    @Transactional(readOnly = true)
    public Optional<Tag> getTagById(Long id) {
        return tagRepository.findById(id);
    }
    

    @Transactional(readOnly = true)
    public Optional<Tag> getTagByName(String name) {
        return tagRepository.findByName(name);
    }
    

    @Transactional(readOnly = true)
    public List<Tag> getAllTags() {
        return tagRepository.findAllOrderByName();
    }
    

    @Transactional(readOnly = true)
    public List<Tag> searchTags(String name) {
        return tagRepository.findByNameContainingIgnoreCase(name);
    }
    

    @Transactional(readOnly = true)
    public List<Tag> getTagsByArtId(Long artId) {
        return tagRepository.findByArtId(artId);
    }
    

    @Transactional(readOnly = true)
    public List<Tag> getPopularTags(int limit) {
        return tagRepository.findPopularTags();
    }
    

    public Tag updateTag(Long id, String newName) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + id));
        

        if (!tag.getName().equals(newName) && tagRepository.existsByName(newName)) {
            throw new RuntimeException("Tag with name '" + newName + "' already exists");
        }
        
        tag.setName(newName);
        return tagRepository.save(tag);
    }
    
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + id));
        tagRepository.delete(tag);
    }
    

    public Tag findOrCreateTag(String name) {
        return tagRepository.findByName(name)
                .orElseGet(() -> {
                    Tag newTag = new Tag(name);
                    return tagRepository.save(newTag);
                });
    }

    public List<Tag> createTags(List<String> tagNames) {
        return tagNames.stream()
                .map(this::findOrCreateTag)
                .toList();
    }
    

    @Transactional(readOnly = true)
    public boolean tagExists(String name) {
        return tagRepository.existsByName(name);
    }

    @Transactional(readOnly = true)
    public Optional<TagDto> getTagDtoById(Long id) {
        return tagRepository.findById(id)
                .map(TagDto::new);
    }

    @Transactional(readOnly = true)
    public Optional<TagDto> getTagDtoByName(String name) {
        return tagRepository.findByName(name)
                .map(TagDto::new);
    }

    @Transactional(readOnly = true)
    public List<TagDto> getAllTagDtos() {
        return tagRepository.findAllOrderByName()
                .stream()
                .map(tag -> {
                    TagDto dto = new TagDto(tag);
                    Long artCount = tagRepository.countArtsByTagId(tag.getId());
                    dto.setArtCount(artCount != null ? artCount.intValue() : 0);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TagDto> searchTagDtos(String name) {
        return tagRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(TagDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TagDto> getTagDtosByArtId(Long artId) {
        return tagRepository.findByArtId(artId)
                .stream()
                .map(TagDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TagDto> getPopularTagDtos() {
        return tagRepository.findPopularTags()
                .stream()
                .map(TagDto::new)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public Long getArtCountByTagId(Long tagId) {
        return tagRepository.countArtsByTagId(tagId);
}
}
