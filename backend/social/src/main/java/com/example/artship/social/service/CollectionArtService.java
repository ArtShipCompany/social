package com.example.artship.social.service;

import com.example.artship.social.dto.ArtDto;
import com.example.artship.social.dto.CollectionArtDto;
import com.example.artship.social.model.Art;
import com.example.artship.social.model.Collection;
import com.example.artship.social.model.CollectionArt;
import com.example.artship.social.model.enumclass.ArtStatus;
import com.example.artship.social.repository.ArtRepository;
import com.example.artship.social.repository.CollectionArtRepository;
import com.example.artship.social.repository.CollectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CollectionArtService {
    
    private static final Logger log = LoggerFactory.getLogger(CollectionArtService.class);
    
    private final CollectionArtRepository collectionArtRepository;
    private final CollectionRepository collectionRepository;
    private final ArtRepository artRepository;
    private final ArtService artService;
    
    public CollectionArtService(CollectionArtRepository collectionArtRepository,
                              CollectionRepository collectionRepository,
                              ArtRepository artRepository,
                              ArtService artService) {
        this.collectionArtRepository = collectionArtRepository;
        this.collectionRepository = collectionRepository;
        this.artRepository = artRepository;
        this.artService = artService;
    }
    
    public CollectionArtDto addArtToCollection(Long collectionId, Long artId) {
        log.info("Adding art {} to collection {}", artId, collectionId);
        
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found with id: " + collectionId));
        
        Art art = artRepository.findById(artId)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + artId));
        
        if (!art.getIsPublicFlag() && !art.getAuthor().getId().equals(collection.getUser().getId())) {
            throw new RuntimeException("Cannot add private art to collection");
        }
        
        if (art.getStatus() != ArtStatus.ACTIVE) {
            throw new RuntimeException("Cannot add art with status: " + art.getStatus());
        }
        
        if (collectionArtRepository.existsByCollectionIdAndArtId(collectionId, artId)) {
            throw new RuntimeException("Art already exists in collection");
        }
        
        CollectionArt collectionArt = new CollectionArt(collection, art);
        CollectionArt savedCollectionArt = collectionArtRepository.save(collectionArt);
        
        if (collection.getCoverImageUrl() == null || collection.getCoverImageUrl().isEmpty()) {
            collection.setCoverImageUrl(art.getImageUrl());
            collectionRepository.save(collection);
            log.info("Set cover image for collection {} from art {}", collectionId, artId);
        }
        
        return new CollectionArtDto(savedCollectionArt);
    }
    
    public void removeArtFromCollection(Long collectionId, Long artId) {
        log.info("Removing art {} from collection {}", artId, collectionId);
        
        if (!collectionArtRepository.existsByCollectionIdAndArtId(collectionId, artId)) {
            log.warn("Art {} not found in collection {}", artId, collectionId);
            return;
        }
        
        collectionArtRepository.deleteByCollectionIdAndArtId(collectionId, artId);
        updateCollectionCoverIfNeeded(collectionId);
    }
    
    public void removeAllArtsFromCollection(Long collectionId) {
        log.info("Removing all arts from collection {}", collectionId);
        
        Long count = collectionArtRepository.countByCollectionId(collectionId);
        collectionArtRepository.deleteByCollectionId(collectionId);
        
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found with id: " + collectionId));
        
        collection.setCoverImageUrl(null);
        collectionRepository.save(collection);
        
        log.info("Removed {} arts from collection {}", count, collectionId);
    }
    
    private void updateCollectionCoverIfNeeded(Long collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found with id: " + collectionId));
        
        if (collection.getCoverImageUrl() == null) {
            return;
        }
        
        List<CollectionArt> arts = collectionArtRepository.findByCollectionId(collectionId);
        
        if (arts.isEmpty()) {
            collection.setCoverImageUrl(null);
            collectionRepository.save(collection);
            log.info("Reset cover image for collection {}", collectionId);
        } else {
            String newCoverUrl = arts.get(0).getArt().getImageUrl();
            if (!newCoverUrl.equals(collection.getCoverImageUrl())) {
                collection.setCoverImageUrl(newCoverUrl);
                collectionRepository.save(collection);
                log.info("Updated cover image for collection {}", collectionId);
            }
        }
    }
    
    @Transactional(readOnly = true)
    public Page<ArtDto> getArtsByCollectionId(Long collectionId, Pageable pageable) {
        Page<CollectionArt> collectionArtsPage = collectionArtRepository.findByCollectionIdAndArtStatus(
            collectionId, ArtStatus.ACTIVE, pageable);
        
        List<ArtDto> artDtos = collectionArtsPage.getContent().stream()
                .map(CollectionArt::getArt)
                .filter(art -> art != null)
                .map(art -> artService.convertToDto(art))
                .collect(Collectors.toList());
        
        return new PageImpl<>(artDtos, pageable, collectionArtsPage.getTotalElements());
    }
    
    @Transactional(readOnly = true)
    public Page<CollectionArtDto> getCollectionsByArtId(Long artId, Pageable pageable) {
        Page<CollectionArt> collectionArtsPage = collectionArtRepository.findByArtId(artId, pageable);
        
        List<CollectionArtDto> dtos = collectionArtsPage.getContent().stream()
                .map(CollectionArtDto::new)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, collectionArtsPage.getTotalElements());
    }
    
    @Transactional(readOnly = true)
    public Page<CollectionArtDto> getCollectionArtDtosByCollectionId(Long collectionId, Pageable pageable) {
        Page<CollectionArt> collectionArtsPage = collectionArtRepository.findByCollectionId(collectionId, pageable);
        
        List<CollectionArtDto> dtos = collectionArtsPage.getContent().stream()
                .map(CollectionArtDto::new)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, collectionArtsPage.getTotalElements());
    }
    
    @Transactional(readOnly = true)
    public List<ArtDto> getAllArtsByCollectionId(Long collectionId) {
        List<CollectionArt> collectionArts = collectionArtRepository.findByCollectionId(collectionId);
        
        if (collectionArts == null || collectionArts.isEmpty()) {
            return Collections.emptyList();
        }
        
        return collectionArts.stream()
                .map(CollectionArt::getArt)
                .filter(art -> art != null && art.getStatus() == ArtStatus.ACTIVE)
                .map(artService::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CollectionArtDto> getCollectionsByArtId(Long artId) {
        List<CollectionArt> collectionArts = collectionArtRepository.findByArtId(artId);
        return collectionArts.stream()
                .map(CollectionArtDto::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CollectionArtDto> getCollectionArtDtosByCollectionId(Long collectionId) {
        List<CollectionArt> collectionArts = collectionArtRepository.findByCollectionId(collectionId);
        return collectionArts.stream()
                .map(CollectionArtDto::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Long getArtCountByCollectionId(Long collectionId) {
        return collectionArtRepository.countByCollectionIdAndArtStatus(collectionId, ArtStatus.ACTIVE);
    }
    
    @Transactional(readOnly = true)
    public boolean existsByCollectionIdAndArtId(Long collectionId, Long artId) {
        return collectionArtRepository.existsByCollectionIdAndArtId(collectionId, artId);
    }
    
    public void moveArtBetweenCollections(Long artId, Long fromCollectionId, Long toCollectionId) {
        if (fromCollectionId.equals(toCollectionId)) {
            throw new RuntimeException("Source and target collections are the same");
        }
        
        if (!collectionArtRepository.existsByCollectionIdAndArtId(fromCollectionId, artId)) {
            throw new RuntimeException("Art not found in source collection");
        }
        
        if (collectionArtRepository.existsByCollectionIdAndArtId(toCollectionId, artId)) {
            throw new RuntimeException("Art already exists in target collection");
        }
        
        collectionArtRepository.deleteByCollectionIdAndArtId(fromCollectionId, artId);
        updateCollectionCoverIfNeeded(fromCollectionId);
        addArtToCollection(toCollectionId, artId);
    }
    
    public CollectionArtDto copyArtToCollection(Long artId, Long collectionId) {
        return addArtToCollection(collectionId, artId);
    }
}