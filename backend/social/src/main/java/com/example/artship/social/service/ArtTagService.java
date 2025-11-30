package com.example.artship.social.service;

import com.example.artship.social.dto.ArtTagDto;
import com.example.artship.social.model.Art;
import com.example.artship.social.model.ArtTag;
import com.example.artship.social.model.Tag;
import com.example.artship.social.repository.ArtRepository;
import com.example.artship.social.repository.ArtTagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class ArtTagService {
    
    private final ArtTagRepository artTagRepository;
    private final ArtRepository artRepository;
    private final TagService tagService;
    
    public ArtTagService(ArtTagRepository artTagRepository, ArtRepository artRepository, TagService tagService) {
        this.artTagRepository = artTagRepository;
        this.artRepository = artRepository;
        this.tagService = tagService;
    }
    
    // Добавление тега к арту
    public void addTagToArt(Long artId, Long tagId) {
        Art art = artRepository.findById(artId)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + artId));
        Tag tag = tagService.getTagById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + tagId));
        
        if (!artTagRepository.existsByArtIdAndTagId(artId, tagId)) {
            ArtTag artTag = new ArtTag(art, tag);
            artTagRepository.save(artTag);
        }
    }
    
    // Добавление тегов к арту по именам
    public void addTagsToArt(Long artId, List<String> tagNames) {
        Art art = artRepository.findById(artId)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + artId));
        
        for (String tagName : tagNames) {
            Tag tag = tagService.findOrCreateTag(tagName);
            if (!artTagRepository.existsByArtIdAndTagId(artId, tag.getId())) {
                ArtTag artTag = new ArtTag(art, tag);
                artTagRepository.save(artTag);
            }
        }
    }
    
    // Удаление тега из арта
    public void removeTagFromArt(Long artId, Long tagId) {
        artTagRepository.deleteByArtIdAndTagId(artId, tagId);
    }
    
    // Удаление всех тегов из арта
    public void removeAllTagsFromArt(Long artId) {
        artTagRepository.deleteByArtId(artId);
    }
    
    // Получение тегов арта
    @Transactional(readOnly = true)
    public List<Tag> getTagsByArtId(Long artId) {
        return artTagRepository.findByArtId(artId).stream()
                .map(ArtTag::getTag)
                .collect(Collectors.toList());
    }
    
    // Получение артов по тегу
    @Transactional(readOnly = true)
    public List<Art> getArtsByTagId(Long tagId) {
        return artTagRepository.findByTagId(tagId).stream()
                .map(ArtTag::getArt)
                .collect(Collectors.toList());
    }
    
    // Получение DTO тегов арта
    @Transactional(readOnly = true)
    public List<Tag> getTagDtosByArtId(Long artId) {
        return getTagsByArtId(artId);
    }
    
    // Проверка существования связи
    @Transactional(readOnly = true)
    public boolean existsByArtIdAndTagId(Long artId, Long tagId) {
        return artTagRepository.existsByArtIdAndTagId(artId, tagId);
    }
    

    @Transactional(readOnly = true)
    public List<ArtTagDto> getArtTagDtosByArtId(Long artId) {
        return artTagRepository.findByArtId(artId).stream()
                .map(ArtTagDto::new)
                .collect(Collectors.toList());
    }
    
    // Получение ArtTagDto по ID тега
    @Transactional(readOnly = true)
    public List<ArtTagDto> getArtTagDtosByTagId(Long tagId) {
        return artTagRepository.findByTagId(tagId).stream()
                .map(ArtTagDto::new)
                .collect(Collectors.toList());
    }
    
    // Получение всех ArtTagDto
    @Transactional(readOnly = true)
    public List<ArtTagDto> getAllArtTagDtos() {
        return artTagRepository.findAll().stream()
                .map(ArtTagDto::new)
                .collect(Collectors.toList());
    }
    
    // Создание связи и возврат DTO
    public ArtTagDto createArtTagWithDto(Long artId, Long tagId) {
        addTagToArt(artId, tagId);
        // Получаем созданную связь
        List<ArtTag> artTags = artTagRepository.findByArtIdAndTagId(artId, tagId);
        if (!artTags.isEmpty()) {
            return new ArtTagDto(artTags.get(0));
        }
        throw new RuntimeException("Failed to create ArtTag relation");
    }
    
    // Получение ArtTagDto по artId и tagId
    @Transactional(readOnly = true)
    public List<ArtTagDto> getArtTagDtosByArtIdAndTagId(Long artId, Long tagId) {
        return artTagRepository.findByArtIdAndTagId(artId, tagId).stream()
                .map(ArtTagDto::new)
                .collect(Collectors.toList());
    }
}