package com.example.artship.social.controller;

import com.example.artship.social.dto.CollectionDto;
import com.example.artship.social.model.User;
import com.example.artship.social.requests.CollectionRequest;
import com.example.artship.social.requests.CollectionUpdateRequest;
import com.example.artship.social.service.CollectionService;
import com.example.artship.social.service.PermissionService;
import com.example.artship.social.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/collections")
@Tag(name = "Collection Controller", description = "API для управления коллекциями")
public class CollectionController {
    
    private static final Logger logger = LoggerFactory.getLogger(CollectionController.class);
    
    private final CollectionService collectionService;
    private final UserService userService;
    private final PermissionService permissionService;
    
    public CollectionController(CollectionService collectionService, 
                                UserService userService,
                                PermissionService permissionService) {
        this.collectionService = collectionService;
        this.userService = userService;
        this.permissionService = permissionService;
    }
        
    // Создание коллекции
    @Operation(summary = "Создать новую коллекцию")
    @PostMapping
    public ResponseEntity<CollectionDto> createCollection(
            @RequestBody CollectionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("=== СОЗДАНИЕ КОЛЛЕКЦИИ ===");
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        
        try {
            CollectionDto collection = collectionService.createCollection(
                request.getTitle(),
                request.getDescription(),
                request.getIsPublic(),
                request.getCoverImageUrl(),
                currentUser.getId()  // Используем ID текущего пользователя
            );
            return new ResponseEntity<>(collection, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            logger.error("Ошибка создания коллекции: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    
    // Получение коллекции по ID (с проверкой прав)
    @Operation(summary = "Получить коллекцию по ID")
    @GetMapping("/{id}")
    public ResponseEntity<CollectionDto> getCollection(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("Получение коллекции ID: {}", id);
        
        Optional<CollectionDto> collectionOpt = collectionService.getCollectionDtoById(id);
        if (collectionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        CollectionDto collection = collectionOpt.get();
        
        User currentUser = null;
        if (userDetails != null) {
            Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
            currentUser = userOpt.orElse(null);
        }
        
        boolean canView = false;
        
        if (collection.getIsPublic()) {
            canView = true;
        }
        else if (currentUser != null) {
            if (permissionService.isAdmin(currentUser) || permissionService.isModerator(currentUser)) {
                canView = true;
                logger.info("{} {} просматривает приватную коллекцию {}", 
                    currentUser.getUserRole(), currentUser.getUsername(), id);
            }
            else if (collection.getUserId().equals(currentUser.getId())) {
                canView = true;
            }
        }
        
        if (!canView) {
            logger.warn("Нет доступа к коллекции {}", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(collection);
    }
    
    // Коллекции пользователя (с пагинацией)
    @Operation(summary = "Получить все коллекции пользователя")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<CollectionDto>> getUserCollections(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("Получение коллекций пользователя ID: {}", userId);
        
        User currentUser = null;
        if (userDetails != null) {
            Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
            currentUser = userOpt.orElse(null);
        }
        
        if (currentUser != null && (permissionService.isAdmin(currentUser) || permissionService.isModerator(currentUser))) {
            logger.info("{} {} запрашивает все коллекции пользователя {}", 
                currentUser.getUserRole(), currentUser.getUsername(), userId);
            Page<CollectionDto> collections = collectionService.getCollectionsByUserId(userId, pageable);
            return ResponseEntity.ok(collections);
        }
        

        if (currentUser != null && currentUser.getId().equals(userId)) {
            // Свои коллекции - все
            Page<CollectionDto> collections = collectionService.getCollectionsByUserId(userId, pageable);
            return ResponseEntity.ok(collections);
        } else {
            // Чужие - только публичные
            Page<CollectionDto> collections = collectionService.getPublicCollectionsByUserId(userId, pageable);
            return ResponseEntity.ok(collections);
        }
    }
    
    // Публичные коллекции пользователя (с пагинацией)
    @Operation(summary = "Получить публичные коллекции пользователя")
    @GetMapping("/user/{userId}/public")
    public ResponseEntity<Page<CollectionDto>> getUserPublicCollections(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CollectionDto> collections = collectionService.getPublicCollectionsByUserId(userId, pageable);
        return ResponseEntity.ok(collections);
    }
    
    // Все публичные коллекции (с пагинацией)
    @Operation(summary = "Получить все публичные коллекции")
    @GetMapping("/public")
    public ResponseEntity<Page<CollectionDto>> getPublicCollections(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CollectionDto> collections = collectionService.getPublicCollections(pageable);
        return ResponseEntity.ok(collections);
    }
    
    // Поиск публичных коллекций (с пагинацией)
    @Operation(summary = "Поиск публичных коллекций")
    @GetMapping("/search")
    public ResponseEntity<Page<CollectionDto>> searchCollections(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CollectionDto> collections = collectionService.searchPublicCollections(q, pageable);
        return ResponseEntity.ok(collections);
    }
    
    
    // Обновление коллекции (с проверкой прав)
    @Operation(summary = "Обновить коллекцию")
    @PutMapping("/{id}")
    public ResponseEntity<CollectionDto> updateCollection(
            @PathVariable Long id,
            @RequestBody CollectionUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("=== ОБНОВЛЕНИЕ КОЛЛЕКЦИИ ID: {} ===", id);
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        
        Optional<CollectionDto> collectionOpt = collectionService.getCollectionDtoById(id);
        if (collectionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        CollectionDto existingCollection = collectionOpt.get();
        
        boolean canEdit = permissionService.isAdmin(currentUser) || 
                          permissionService.isModerator(currentUser) ||
                          existingCollection.getUserId().equals(currentUser.getId());
        
        if (!canEdit) {
            logger.warn("Пользователь {} не имеет прав на редактирование коллекции {}", 
                       currentUser.getUsername(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        if (permissionService.isAdmin(currentUser) || permissionService.isModerator(currentUser)) {
            logger.info("{} {} редактирует коллекцию {}", 
                currentUser.getUserRole(), currentUser.getUsername(), id);
        }
        
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
            logger.error("Ошибка обновления коллекции: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
        
    // Удаление коллекции (с проверкой прав)
    @Operation(summary = "Удалить коллекцию")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("=== УДАЛЕНИЕ КОЛЛЕКЦИИ ID: {} ===", id);
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        
        Optional<CollectionDto> collectionOpt = collectionService.getCollectionDtoById(id);
        if (collectionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        CollectionDto existingCollection = collectionOpt.get();
        
        boolean canDelete = permissionService.isAdmin(currentUser) || 
                            permissionService.isModerator(currentUser) ||
                            existingCollection.getUserId().equals(currentUser.getId());
        
        if (!canDelete) {
            logger.warn("Пользователь {} не имеет прав на удаление коллекции {}", 
                       currentUser.getUsername(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        if (permissionService.isAdmin(currentUser) || permissionService.isModerator(currentUser)) {
            logger.info("{} {} удаляет коллекцию {}", 
                currentUser.getUserRole(), currentUser.getUsername(), id);
        }
        
        try {
            collectionService.deleteCollection(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Ошибка удаления коллекции: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    
}