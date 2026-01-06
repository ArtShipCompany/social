package com.example.artship.social.service;

import com.example.artship.social.dto.ArtTagDto;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ArtTagService {
    
    private static final Logger log = LoggerFactory.getLogger(ArtTagService.class);
    
    private final ArtTagRepository artTagRepository;
    private final ArtRepository artRepository;
    private final TagRepository tagRepository;
    private final TagManagementService tagManagementService; // Используем TagManagementService
    
    public ArtTagService(ArtTagRepository artTagRepository,
                        ArtRepository artRepository,
                        TagRepository tagRepository,
                        TagManagementService tagManagementService) { // Убираем ArtService
        this.artTagRepository = artTagRepository;
        this.artRepository = artRepository;
        this.tagRepository = tagRepository;
        this.tagManagementService = tagManagementService;
    }
    
    // Создание связи арт-тег (возвращает ArtTagDto)
    public ArtTagDto createArtTag(Long artId, Long tagId) {
        log.info("Creating art-tag relation: artId={}, tagId={}", artId, tagId);
        
        Art art = artRepository.findById(artId)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + artId));
        
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + tagId));
        
        if (artTagRepository.existsByArtIdAndTagId(artId, tagId)) {
            throw new RuntimeException("Art-tag relation already exists");
        }
        
        ArtTag artTag = new ArtTag(art, tag);
        ArtTag savedArtTag = artTagRepository.save(artTag);
        
        log.info("Art-tag relation created successfully");
        return new ArtTagDto(savedArtTag);
    }
    
    // Удаление связи
    public void removeTagFromArt(Long artId, Long tagId) {
        tagManagementService.removeTagFromArt(artId, tagId);
    }
    
    // Проверка существования связи
    public boolean existsByArtIdAndTagId(Long artId, Long tagId) {
        return tagManagementService.existsByArtIdAndTagId(artId, tagId);
    }
    
    // Получение всех ArtTagDto для арта
    public List<ArtTagDto> getArtTagDtosByArtId(Long artId) {
        return artTagRepository.findByArtId(artId).stream()
                .map(ArtTagDto::new)
                .collect(Collectors.toList());
    }
    
    // Получение всех ArtTagDto для тега
    public List<ArtTagDto> getArtTagDtosByTagId(Long tagId) {
        return artTagRepository.findByTagId(tagId).stream()
                .map(ArtTagDto::new)
                .collect(Collectors.toList());
    }
    
    // Количество артов по тегу
    public Long getArtCountByTagId(Long tagId) {
        return tagManagementService.getArtCountByTagId(tagId);
    }
    
    // Количество тегов у арта
    public Long getTagCountByArtId(Long artId) {
        return tagManagementService.getTagCountByArtId(artId);
    }
    
    // Удаление всех тегов из арта
    public void removeAllTagsFromArt(Long artId) {
        tagManagementService.removeAllTagsFromArt(artId);
    }
}