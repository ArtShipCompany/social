package com.example.artship.social.controller;

import com.example.artship.social.dto.CollectionDto;
import com.example.artship.social.service.CollectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {
    
    private final CollectionService collectionService;
    
    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }
    
    // Создание коллекции
    @PostMapping
    public ResponseEntity<CollectionDto> createCollection(@RequestBody CollectionRequest request) {
        try {
            CollectionDto collection = collectionService.createCollection(
                request.getTitle(),
                request.getDescription(),
                request.getIsPublic(),
                request.getCoverImageUrl(),
                request.getUserId()
            );
            return new ResponseEntity<>(collection, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Получение коллекции по ID
    @GetMapping("/{id}")
    public ResponseEntity<CollectionDto> getCollection(@PathVariable Long id) {
        Optional<CollectionDto> collection = collectionService.getCollectionDtoById(id);
        return collection.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }
    
    // Обновление коллекции
    @PutMapping("/{id}")
    public ResponseEntity<CollectionDto> updateCollection(
            @PathVariable Long id,
            @RequestBody CollectionUpdateRequest request) {
        try {
            CollectionDto collection = collectionService.updateCollection(
                id,
                request.getTitle(),
                request.getDescription(),
                request.getIsPublic(),
                request.getCoverImageUrl()
            );
            return ResponseEntity.ok(collection);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Удаление коллекции
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable Long id) {
        try {
            collectionService.deleteCollection(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Коллекции пользователя
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CollectionDto>> getUserCollections(@PathVariable Long userId) {
        List<CollectionDto> collections = collectionService.getCollectionsByUserId(userId);
        return ResponseEntity.ok(collections);
    }
    
    // Публичные коллекции пользователя
    @GetMapping("/user/{userId}/public")
    public ResponseEntity<List<CollectionDto>> getUserPublicCollections(@PathVariable Long userId) {
        List<CollectionDto> collections = collectionService.getPublicCollectionsByUserId(userId);
        return ResponseEntity.ok(collections);
    }
    
    // Все публичные коллекции
    @GetMapping("/public")
    public ResponseEntity<List<CollectionDto>> getPublicCollections() {
        List<CollectionDto> collections = collectionService.getPublicCollections();
        return ResponseEntity.ok(collections);
    }
    
    // Поиск публичных коллекций
    @GetMapping("/search")
    public ResponseEntity<List<CollectionDto>> searchCollections(@RequestParam String q) {
        List<CollectionDto> collections = collectionService.searchPublicCollections(q);
        return ResponseEntity.ok(collections);
    }
    
    // Проверка прав доступа
    @GetMapping("/{id}/user/{userId}/access")
    public ResponseEntity<Boolean> checkCollectionAccess(
            @PathVariable Long id,
            @PathVariable Long userId) {
        boolean hasAccess = collectionService.isUserOwnerOfCollection(id, userId);
        return ResponseEntity.ok(hasAccess);
    }
    
    // DTO для создания коллекции
    public static class CollectionRequest {
        private String title;
        private String description;
        private Boolean isPublic;
        private String coverImageUrl;
        private Long userId;
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Boolean getIsPublic() { return isPublic; }
        public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
        
        public String getCoverImageUrl() { return coverImageUrl; }
        public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
    

    public static class CollectionUpdateRequest {
        private String title;
        private String description;
        private Boolean isPublic;
        private String coverImageUrl;
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Boolean getIsPublic() { return isPublic; }
        public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
        
        public String getCoverImageUrl() { return coverImageUrl; }
        public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    }
}