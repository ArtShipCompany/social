package com.example.artship.social.controller;

import com.example.artship.social.dto.CollectionDto;
import com.example.artship.social.requests.CollectionRequest;
import com.example.artship.social.requests.CollectionUpdateRequest;
import com.example.artship.social.service.CollectionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    
    // Коллекции пользователя (с пагинацией)
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<CollectionDto>> getUserCollections(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CollectionDto> collections = collectionService.getCollectionsByUserId(userId, pageable);
        return ResponseEntity.ok(collections);
    }
    
    // Публичные коллекции пользователя (с пагинацией)
    @GetMapping("/user/{userId}/public")
    public ResponseEntity<Page<CollectionDto>> getUserPublicCollections(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CollectionDto> collections = collectionService.getPublicCollectionsByUserId(userId, pageable);
        return ResponseEntity.ok(collections);
    }
    
    // Все публичные коллекции (с пагинацией)
    @GetMapping("/public")
    public ResponseEntity<Page<CollectionDto>> getPublicCollections(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CollectionDto> collections = collectionService.getPublicCollections(pageable);
        return ResponseEntity.ok(collections);
    }
    
    // Поиск публичных коллекций (с пагинацией)
    @GetMapping("/search")
    public ResponseEntity<Page<CollectionDto>> searchCollections(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CollectionDto> collections = collectionService.searchPublicCollections(q, pageable);
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
}