package com.example.artship.social.service;

import com.example.artship.social.dto.ArtDto;
import com.example.artship.social.model.Collection;
import com.example.artship.social.model.User;
import com.example.artship.social.repository.CollectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikedArtService {
    
    private static final Logger log = LoggerFactory.getLogger(LikedArtService.class);
    
    private static final String LIKED_COLLECTION_NAME = "Понравившиеся";
    
    private final CollectionService collectionService;
    private final CollectionArtService collectionArtService;
    private final CollectionRepository collectionRepository;
    
    public LikedArtService(CollectionService collectionService,
                           CollectionArtService collectionArtService,
                           CollectionRepository collectionRepository) {
        this.collectionService = collectionService;
        this.collectionArtService = collectionArtService;
        this.collectionRepository = collectionRepository;
    }
    
    //Получить или создать коллекцию "Понравившиеся" для пользователя
    private Collection getOrCreateLikedCollection(Long userId) {
        return collectionRepository.findByUserIdAndTitle(userId, LIKED_COLLECTION_NAME)
            .orElseGet(() -> {
                log.info("Creating 'Liked' collection for user ID: {}", userId);
                return collectionRepository.save(
                    new Collection(LIKED_COLLECTION_NAME, new User(userId))
                );
            });
    }
    
    // Добавить арт в "Понравившиеся"
    @Transactional
    public void addArtToLiked(Long userId, Long artId) {
        log.info("User {} likes art {}", userId, artId);
        
        Collection likedCollection = getOrCreateLikedCollection(userId);
        
        try {
            collectionArtService.addArtToCollection(likedCollection.getId(), artId);
            log.info("Art {} added to user {} liked collection", artId, userId);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("already exists")) {
                log.warn("Art {} already in liked collection", artId);
                throw new RuntimeException("Art already liked");
            }
            throw e;
        }
    }
    
    // Удалить арт из "Понравившихся"
    @Transactional
    public void removeArtFromLiked(Long userId, Long artId) {
        log.info("User {} unlikes art {}", userId, artId);
        
        Collection likedCollection = getOrCreateLikedCollection(userId);
        
        if (collectionArtService.existsByCollectionIdAndArtId(likedCollection.getId(), artId)) {
            collectionArtService.removeArtFromCollection(likedCollection.getId(), artId);
            log.info("Art {} removed from user {} liked collection", artId, userId);
        } else {
            log.warn("Art {} not in liked collection", artId);
            throw new RuntimeException("Art not in liked collection");
        }
    }
    
    // Проверить, добавлен ли арт в "Понравившиеся"
    @Transactional(readOnly = true)
    public boolean isArtLikedByUser(Long userId, Long artId) {
        return collectionRepository.findByUserIdAndTitle(userId, LIKED_COLLECTION_NAME)
            .map(collection -> collectionArtService.existsByCollectionIdAndArtId(collection.getId(), artId))
            .orElse(false);
    }
    
    //Получить все "Понравившиеся" арты пользователя (с пагинацией)
    @Transactional(readOnly = true)
    public Page<ArtDto> getLikedArtsByUser(Long userId, Pageable pageable) {
        log.debug("Getting liked arts for user ID: {}", userId);
        
        return collectionRepository.findByUserIdAndTitle(userId, LIKED_COLLECTION_NAME)
            .map(collection -> collectionArtService.getArtsByCollectionId(collection.getId(), pageable))
            .orElse(Page.empty(pageable));
    }
    
    //Получить количество "Понравившихся" артов пользователя
    @Transactional(readOnly = true)
    public long getLikedArtsCount(Long userId) {
        return collectionRepository.findByUserIdAndTitle(userId, LIKED_COLLECTION_NAME)
            .map(collection -> collectionArtService.getArtCountByCollectionId(collection.getId()))
            .orElse(0L);
    }
    
    // Удалить все "Понравившиеся" арты пользователя (при удалении аккаунта)
    
    @Transactional
    public void deleteAllLikedArtsByUser(Long userId) {
        log.info("Deleting all liked arts for user ID: {}", userId);
        
        collectionRepository.findByUserIdAndTitle(userId, LIKED_COLLECTION_NAME)
            .ifPresent(collection -> {
                collectionArtService.removeAllArtsFromCollection(collection.getId());
                collectionRepository.delete(collection);
                log.info("Deleted liked collection for user ID: {}", userId);
            });
    }
}