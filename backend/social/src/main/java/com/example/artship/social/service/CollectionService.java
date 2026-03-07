package com.example.artship.social.service;

import com.example.artship.social.dto.ArtDto;
import com.example.artship.social.dto.CollectionDto;
import com.example.artship.social.model.Collection;
import com.example.artship.social.model.User;
import com.example.artship.social.repository.CollectionRepository;
import com.example.artship.social.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CollectionService {
    
    private static final Logger log = LoggerFactory.getLogger(CollectionService.class);
    
    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final CollectionArtService collectionArtService;
    
    public CollectionService(CollectionRepository collectionRepository, 
                           UserRepository userRepository,
                           CollectionArtService collectionArtService) {
        this.collectionRepository = collectionRepository;
        this.userRepository = userRepository;
        this.collectionArtService = collectionArtService;
    }
    
    // Создание коллекции
    public CollectionDto createCollection(String title, String description, Boolean isPublic, 
                                        String coverImageUrl, Long userId) {
        log.info("Creating collection: title='{}', userId={}", title, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (collectionRepository.existsByUserIdAndTitle(userId, title)) {
            throw new RuntimeException("Collection with title '" + title + "' already exists for this user");
        }
        
        Collection collection = new Collection(title, user);
        collection.setDescription(description);
        collection.setIsPublic(isPublic != null ? isPublic : true);
        collection.setCoverImageUrl(coverImageUrl);
        
        Collection savedCollection = collectionRepository.save(collection);
        log.info("Collection created with ID: {}", savedCollection.getId());
        
        return convertToDtoWithArts(savedCollection);
    }
    
    // Получение коллекции по ID
    @Transactional(readOnly = true)
    public Optional<Collection> getCollectionById(Long id) {
        return collectionRepository.findById(id);
    }
    
    // Получение DTO коллекции по ID (БЕЗ артов для списков)
    @Transactional(readOnly = true)
    public Optional<CollectionDto> getCollectionDtoById(Long id) {
        return collectionRepository.findById(id)
                .map(this::convertToDtoWithoutArts);
    }
    
    // Получение DTO коллекции по ID С артами
    @Transactional(readOnly = true)
    public Optional<CollectionDto> getCollectionDtoWithArtsById(Long id) {
        log.debug("Getting collection with arts for ID: {}", id);
        
        return collectionRepository.findById(id)
                .map(collection -> {
                    List<ArtDto> arts = collectionArtService.getArtsByCollectionId(collection.getId());

                    CollectionDto dto = new CollectionDto();
                    dto.setId(collection.getId());
                    dto.setTitle(collection.getTitle());
                    dto.setDescription(collection.getDescription());
                    dto.setIsPublic(collection.getIsPublic());
                    dto.setCoverImageUrl(collection.getCoverImageUrl());
                    dto.setCreatedAt(collection.getCreatedAt());
                    dto.setUserId(collection.getUser() != null ? collection.getUser().getId() : null);
                    dto.setUsername(collection.getUser() != null ? collection.getUser().getUsername() : null);
                    dto.setArtCount(arts.size());
                    dto.setArts(arts);
                    
                    log.debug("Collection {} has {} arts", id, arts.size());
                    return dto;
                });
    }
    
    // Обновление коллекции
    public CollectionDto updateCollection(Long id, String title, String description, 
                                        Boolean isPublic, String coverImageUrl) {
        log.info("Updating collection {} with title: {}", id, title);
        
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found with id: " + id));
        
        if (title != null && !title.equals(collection.getTitle())) {
            if (collectionRepository.existsByUserIdAndTitle(collection.getUser().getId(), title)) {
                throw new RuntimeException("Collection with title '" + title + "' already exists for this user");
            }
            collection.setTitle(title);
        }
        
        if (description != null) {
            collection.setDescription(description);
        }
        
        if (isPublic != null) {
            collection.setIsPublic(isPublic);
        }
        
        if (coverImageUrl != null) {
            collection.setCoverImageUrl(coverImageUrl);
        }
        
        Collection updatedCollection = collectionRepository.save(collection);
        log.info("Collection {} updated successfully", id);
        
        return convertToDtoWithArts(updatedCollection);
    }
    
    // Удаление коллекции
    public void deleteCollection(Long id) {
        log.info("Deleting collection with ID: {}", id);
        
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found with id: " + id));
        
        collectionArtService.removeAllArtsFromCollection(id);
        collectionRepository.delete(collection);
        
        log.info("Collection {} deleted successfully", id);
    }
    
    // Коллекции пользователя (БЕЗ артов для списка)
    @Transactional(readOnly = true)
    public List<CollectionDto> getCollectionsByUserId(Long userId) {
        log.debug("Getting collections for user {}", userId);
        
        return collectionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDtoWithoutArts)
                .collect(Collectors.toList());
    }
    
    // Коллекции пользователя С артами
    @Transactional(readOnly = true)
    public List<CollectionDto> getCollectionsWithArtsByUserId(Long userId) {
        log.debug("Getting collections with arts for user {}", userId);
        
        List<Collection> collections = collectionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return collections.stream()
                .map(collection -> {
                    // Получаем арты для каждой коллекции
                    List<ArtDto> arts = collectionArtService.getArtsByCollectionId(collection.getId());
                    
                    // Создаем DTO с артами
                    CollectionDto dto = new CollectionDto();
                    dto.setId(collection.getId());
                    dto.setTitle(collection.getTitle());
                    dto.setDescription(collection.getDescription());
                    dto.setIsPublic(collection.getIsPublic());
                    dto.setCoverImageUrl(collection.getCoverImageUrl());
                    dto.setCreatedAt(collection.getCreatedAt());
                    dto.setUserId(collection.getUser() != null ? collection.getUser().getId() : null);
                    dto.setUsername(collection.getUser() != null ? collection.getUser().getUsername() : null);
                    dto.setArtCount(arts.size());
                    dto.setArts(arts);
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    // Публичные коллекции пользователя
    @Transactional(readOnly = true)
    public List<CollectionDto> getPublicCollectionsByUserId(Long userId) {
        return collectionRepository.findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDtoWithoutArts)
                .collect(Collectors.toList());
    }
    
    // Все публичные коллекции
    @Transactional(readOnly = true)
    public List<CollectionDto> getPublicCollections() {
        return collectionRepository.findByIsPublicTrueOrderByCreatedAtDesc().stream()
                .map(this::convertToDtoWithoutArts)
                .collect(Collectors.toList());
    }
    
    // Поиск публичных коллекций
    @Transactional(readOnly = true)
    public List<CollectionDto> searchPublicCollections(String query) {
        return collectionRepository.searchPublicCollections(query).stream()
                .map(this::convertToDtoWithoutArts)
                .collect(Collectors.toList());
    }
    
    // Проверка прав доступа
    @Transactional(readOnly = true)
    public boolean isUserOwnerOfCollection(Long collectionId, Long userId) {
        return collectionRepository.findById(collectionId)
                .map(collection -> collection.getUser().getId().equals(userId))
                .orElse(false);
    }
    
    private CollectionDto convertToDtoWithoutArts(Collection collection) {
        CollectionDto dto = new CollectionDto();
        dto.setId(collection.getId());
        dto.setTitle(collection.getTitle());
        dto.setDescription(collection.getDescription());
        dto.setIsPublic(collection.getIsPublic());
        dto.setCoverImageUrl(collection.getCoverImageUrl());
        dto.setCreatedAt(collection.getCreatedAt());
        dto.setUserId(collection.getUser() != null ? collection.getUser().getId() : null);
        dto.setUsername(collection.getUser() != null ? collection.getUser().getUsername() : null);
        
        // Устанавливаем количество артов без загрузки самих артов
        Long artCount = collectionArtService.getArtCountByCollectionId(collection.getId());
        dto.setArtCount(artCount != null ? artCount.intValue() : 0);
        dto.setArts(Collections.emptyList()); // Пустой список для списков
        
        return dto;
    }
    
    // Конвертация в DTO С артами
    private CollectionDto convertToDtoWithArts(Collection collection) {
        log.debug("Converting collection {} to DTO with arts", collection.getId());

        List<ArtDto> arts = collectionArtService.getArtsByCollectionId(collection.getId());
 
        CollectionDto dto = new CollectionDto();
        dto.setId(collection.getId());
        dto.setTitle(collection.getTitle());
        dto.setDescription(collection.getDescription());
        dto.setIsPublic(collection.getIsPublic());
        dto.setCoverImageUrl(collection.getCoverImageUrl());
        dto.setCreatedAt(collection.getCreatedAt());
        dto.setUserId(collection.getUser() != null ? collection.getUser().getId() : null);
        dto.setUsername(collection.getUser() != null ? collection.getUser().getUsername() : null);
        dto.setArtCount(arts.size());
        dto.setArts(arts);
        
        log.debug("Collection {} converted, has {} arts", collection.getId(), arts.size());
        return dto;
    }
}