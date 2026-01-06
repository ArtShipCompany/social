package com.example.artship.social.service;

import com.example.artship.social.dto.ArtDto;
import com.example.artship.social.dto.TagDto;
import com.example.artship.social.dto.UserDto;
import com.example.artship.social.model.Art;
import com.example.artship.social.model.ArtTag;
import com.example.artship.social.model.Tag;
import com.example.artship.social.repository.ArtRepository;
import com.example.artship.social.repository.ArtTagRepository;
import com.example.artship.social.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TagManagementService {
    
    private static final Logger log = LoggerFactory.getLogger(TagManagementService.class);
    
    private final ArtRepository artRepository;
    private final TagRepository tagRepository;
    private final ArtTagRepository artTagRepository;
    
    public TagManagementService(ArtRepository artRepository,
                               TagRepository tagRepository,
                               ArtTagRepository artTagRepository) {
        this.artRepository = artRepository;
        this.tagRepository = tagRepository;
        this.artTagRepository = artTagRepository;
    }
    
    // Добавление тегов к арту (массовое)
    public void addTagsToArt(Long artId, List<String> tagNames) {
        log.info("Adding tags to art {}: {}", artId, tagNames);
        
        Art art = artRepository.findById(artId)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + artId));
        
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag(tagName);
                        return tagRepository.save(newTag);
                    });
            
            if (!artTagRepository.existsByArtIdAndTagId(artId, tag.getId())) {
                ArtTag artTag = new ArtTag(art, tag);
                artTagRepository.save(artTag);
                log.debug("Added tag '{}' to art {}", tagName, artId);
            }
        }
        
        log.info("Successfully added {} tags to art {}", tagNames.size(), artId);
    }
    
    // Добавление одного тега к арту
    public void addTagToArt(Long artId, Long tagId) {
        log.info("Adding tag {} to art {}", tagId, artId);
        
        Art art = artRepository.findById(artId)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + artId));
        
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + tagId));
        
        if (artTagRepository.existsByArtIdAndTagId(artId, tagId)) {
            throw new RuntimeException("Tag " + tagId + " is already added to art " + artId);
        }
        
        ArtTag artTag = new ArtTag(art, tag);
        artTagRepository.save(artTag);
        log.info("Successfully added tag {} to art {}", tagId, artId);
    }
    
    // Удаление тега из арта
    public void removeTagFromArt(Long artId, Long tagId) {
        log.info("Removing tag {} from art {}", tagId, artId);
        
        if (!artTagRepository.existsByArtIdAndTagId(artId, tagId)) {
            throw new RuntimeException("Tag not found in art");
        }
        
        artTagRepository.deleteByArtIdAndTagId(artId, tagId);
        log.info("Successfully removed tag {} from art {}", tagId, artId);
    }
    
    // Удаление всех тегов из арта
    public void removeAllTagsFromArt(Long artId) {
        log.info("Removing all tags from art {}", artId);
        
        Long count = artTagRepository.countByArtId(artId);
        artTagRepository.deleteByArtId(artId);
        log.info("Removed {} tags from art {}", count, artId);
    }
    
    // Получение тегов арта
    @Transactional(readOnly = true)
    public List<TagDto> getTagsByArtId(Long artId) {
        log.debug("Getting tags for art {}", artId);
        
        List<ArtTag> artTags = artTagRepository.findByArtId(artId);
        if (artTags.isEmpty()) {
            return Collections.emptyList();
        }
        
        return artTags.stream()
                .map(ArtTag::getTag)
                .map(TagDto::new)
                .collect(Collectors.toList());
    }
    
    // Получение артов по тегу
    @Transactional(readOnly = true)
    public List<ArtDto> getArtsByTagId(Long tagId) {
        log.debug("Getting arts for tag {}", tagId);
        
        List<ArtTag> artTags = artTagRepository.findByTagId(tagId);
        if (artTags.isEmpty()) {
            return Collections.emptyList();
        }
        
        return artTags.stream()
                .map(ArtTag::getArt)
                .map(this::convertToArtDto)
                .collect(Collectors.toList());
    }
    
    // Проверка существования связи
    @Transactional(readOnly = true)
    public boolean existsByArtIdAndTagId(Long artId, Long tagId) {
        return artTagRepository.existsByArtIdAndTagId(artId, tagId);
    }
    
    // Количество тегов у арта
    @Transactional(readOnly = true)
    public Long getTagCountByArtId(Long artId) {
        return artTagRepository.countByArtId(artId);
    }
    
    // Количество артов по тегу
    @Transactional(readOnly = true)
    public Long getArtCountByTagId(Long tagId) {
        return artTagRepository.countByTagId(tagId);
    }
    
    // Конвертация Art в ArtDto (без тегов)
    private ArtDto convertToArtDto(Art art) {
        if (art == null) return null;
        
        ArtDto dto = new ArtDto();
        dto.setId(art.getId());
        dto.setTitle(art.getTitle());
        dto.setDescription(art.getDescription());
        dto.setImage(art.getImageUrl());
        dto.setProjectDataUrl(art.getProjectDataUrl());
        dto.setPublic(art.getIsPublic() != null ? art.getIsPublic() : true);
        dto.setCreatedAt(art.getCreatedAt());
        dto.setUpdatedAt(art.getUpdatedAt());
        
        // Автор
        if (art.getAuthor() != null) {
            UserDto authorDto = new UserDto();
            authorDto.setId(art.getAuthor().getId());
            authorDto.setUsername(art.getAuthor().getUsername());
            authorDto.setEmail(art.getAuthor().getEmail());
            authorDto.setAvatarUrl(art.getAuthor().getAvatarUrl());
            dto.setAuthor(authorDto);
        }
        
        return dto;
    }
}