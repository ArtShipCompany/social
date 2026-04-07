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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@Transactional
public class ArtTagService {
    
    private static final Logger log = LoggerFactory.getLogger(ArtTagService.class);
    
    private final ArtTagRepository artTagRepository;
    private final ArtRepository artRepository;
    private final TagRepository tagRepository;
    private final TagManagementService tagManagementService; 
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

    public Optional<ArtTagDto> getArtTagDtoByArtIdAndTagId(Long artId, Long tagId) {
        log.debug("Getting art-tag relation by artId={} and tagId={}", artId, tagId);
        
        return artTagRepository.findByArtIdAndTagId(artId, tagId)
                .map(this::convertToDto);
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
    public Page<ArtTagDto> getArtTagDtosByArtId(Long artId, Pageable pageable) {
        Page<ArtTag> artTagsPage = artTagRepository.findByArtId(artId, pageable);
        return artTagsPage.map(this::convertToDto);
    }
    
    // Получение всех ArtTagDto для тега
    public Page<ArtTagDto> getArtTagDtosByTagId(Long tagId, Pageable pageable) {
        Page<ArtTag> artTagsPage = artTagRepository.findByTagId(tagId, pageable);
        return artTagsPage.map(this::convertToDto);
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

    private ArtTagDto convertToDto(ArtTag artTag) {
        return new ArtTagDto(artTag);
    }
}