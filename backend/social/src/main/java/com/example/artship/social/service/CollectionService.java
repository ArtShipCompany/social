package com.example.artship.social.service;

import com.example.artship.social.dto.CollectionDto;
import com.example.artship.social.model.Collection;
import com.example.artship.social.model.User;
import com.example.artship.social.repository.CollectionRepository;
import com.example.artship.social.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CollectionService {
    
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
    public CollectionDto createCollection(String title, String description, Boolean isPublic, String coverImageUrl, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Проверяем уникальность названия для пользователя
        if (collectionRepository.existsByUserIdAndTitle(userId, title)) {
            throw new RuntimeException("Collection with title '" + title + "' already exists for this user");
        }
        
        Collection collection = new Collection(title, user);
        collection.setDescription(description);
        collection.setIsPublic(isPublic != null ? isPublic : true);
        collection.setCoverImageUrl(coverImageUrl);
        
        Collection savedCollection = collectionRepository.save(collection);
        return convertToDto(savedCollection);
    }
    
    // Получение коллекции по ID
    @Transactional(readOnly = true)
    public Optional<Collection> getCollectionById(Long id) {
        return collectionRepository.findById(id);
    }
    
    // Получение DTO коллекции по ID
    @Transactional(readOnly = true)
    public Optional<CollectionDto> getCollectionDtoById(Long id) {
        return collectionRepository.findById(id)
                .map(this::convertToDto);
    }
    
    // Обновление коллекции
    public CollectionDto updateCollection(Long id, String title, String description, Boolean isPublic, String coverImageUrl) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found with id: " + id));
        
        if (title != null && !title.equals(collection.getTitle())) {
            // Проверяем уникальность нового названия
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
        return convertToDto(updatedCollection);
    }
    
    // Удаление коллекции
    public void deleteCollection(Long id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found with id: " + id));
        
        // Удаляем все связи с артами
        collectionArtService.removeAllArtsFromCollection(id);
        collectionRepository.delete(collection);
    }
    
    // Коллекции пользователя
    @Transactional(readOnly = true)
    public List<CollectionDto> getCollectionsByUserId(Long userId) {
        return collectionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // Публичные коллекции пользователя
    @Transactional(readOnly = true)
    public List<CollectionDto> getPublicCollectionsByUserId(Long userId) {
        return collectionRepository.findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // Все публичные коллекции
    @Transactional(readOnly = true)
    public List<CollectionDto> getPublicCollections() {
        return collectionRepository.findByIsPublicTrueOrderByCreatedAtDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // Поиск публичных коллекций
    @Transactional(readOnly = true)
    public List<CollectionDto> searchPublicCollections(String query) {
        return collectionRepository.searchPublicCollections(query).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // Проверка прав доступа
    @Transactional(readOnly = true)
    public boolean isUserOwnerOfCollection(Long collectionId, Long userId) {
        return collectionRepository.findById(collectionId)
                .map(collection -> collection.getUser().getId().equals(userId))
                .orElse(false);
    }
    
    // Конвертация в DTO
    private CollectionDto convertToDto(Collection collection) {
        CollectionDto dto = new CollectionDto(collection);
        // Устанавливаем актуальное количество артов
        Long artCount = collectionArtService.getArtCountByCollectionId(collection.getId());
        dto.setArtCount(artCount != null ? artCount.intValue() : 0);
        return dto;
    }
}