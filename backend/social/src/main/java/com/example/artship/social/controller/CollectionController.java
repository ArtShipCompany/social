package com.example.artship.social.controller;

import com.example.artship.social.dto.CollectionDto;
import com.example.artship.social.model.User;
import com.example.artship.social.service.CollectionService;
import com.example.artship.social.service.LocalFileStorageService;
import com.example.artship.social.service.PermissionService;
import com.example.artship.social.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/collections")
@Tag(name = "Collection Controller", description = "API для управления коллекциями")
public class CollectionController {
    
    private static final Logger logger = LoggerFactory.getLogger(CollectionController.class);
    
    private final CollectionService collectionService;
    private final UserService userService;
    private final PermissionService permissionService;
    private final LocalFileStorageService fileStorageService;
    
    public CollectionController(CollectionService collectionService, 
                                UserService userService,
                                PermissionService permissionService,
                                LocalFileStorageService fileStorageService) {
        this.collectionService = collectionService;
        this.userService = userService;
        this.permissionService = permissionService;
        this.fileStorageService = fileStorageService;
    }
    
    @Operation(
        summary = "Создать новую коллекцию",
        description = "Создает коллекцию с возможностью загрузки изображения обложки. Формат запроса: multipart/form-data"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Коллекция успешно создана"),
        @ApiResponse(responseCode = "400", description = "Неверные данные запроса"),
        @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, 
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionDto> createCollection(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(required = false) MultipartFile coverImageFile,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("=== СОЗДАНИЕ КОЛЛЕКЦИИ ===");
        
        if (userDetails == null) {
            logger.error("Пользователь не авторизован");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            logger.error("Пользователь не найден: {}", userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        logger.info("Пользователь: {}, роль: {}", currentUser.getUsername(), currentUser.getUserRole());
        
        if (title == null || title.trim().isEmpty()) {
            logger.error("Заголовок не указан");
            return ResponseEntity.badRequest().build();
        }
        
        String coverImageUrl = null;
        
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            String contentType = coverImageFile.getContentType();
            if (!isValidImageFormat(contentType)) {
                logger.error("Неподдерживаемый формат: {}", contentType);
                return ResponseEntity.badRequest().build();
            }
            
            long fileSize = coverImageFile.getSize();
            if (fileSize > 10 * 1024 * 1024) {
                logger.error("Файл слишком большой: {} байт", fileSize);
                return ResponseEntity.badRequest().build();
            }
            
            coverImageUrl = fileStorageService.uploadFile(coverImageFile);
            logger.info("Обложка загружена: {}", coverImageUrl);
        }
        
        try {
            CollectionDto collection = collectionService.createCollection(
                title.trim(),
                description != null ? description.trim() : null,
                isPublic,
                coverImageUrl,
                currentUser.getId()
            );
            logger.info("=== КОЛЛЕКЦИЯ СОЗДАНА. ID: {} ===", collection.getId());
            return new ResponseEntity<>(collection, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            logger.error("Ошибка создания коллекции: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(
        summary = "Обновить коллекцию",
        description = "Обновляет коллекцию с возможностью загрузки нового изображения обложки. Формат запроса: multipart/form-data"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Коллекция успешно обновлена"),
        @ApiResponse(responseCode = "400", description = "Неверные данные запроса"),
        @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
        @ApiResponse(responseCode = "403", description = "Нет прав на редактирование"),
        @ApiResponse(responseCode = "404", description = "Коллекция не найдена")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
               produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionDto> updateCollection(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(required = false) MultipartFile coverImageFile,
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
            logger.warn("Коллекция с ID {} не найдена", id);
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
        
        String coverImageUrl = existingCollection.getCoverImageUrl();
        
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            String contentType = coverImageFile.getContentType();
            if (!isValidImageFormat(contentType)) {
                logger.error("Неподдерживаемый формат: {}", contentType);
                return ResponseEntity.badRequest().build();
            }
            
            long fileSize = coverImageFile.getSize();
            if (fileSize > 10 * 1024 * 1024) {
                logger.error("Файл слишком большой: {} байт", fileSize);
                return ResponseEntity.badRequest().build();
            }
            
            if (existingCollection.getCoverImageUrl() != null) {
                try {
                    fileStorageService.deleteFile(existingCollection.getCoverImageUrl());
                    logger.info("Старая обложка удалена: {}", existingCollection.getCoverImageUrl());
                } catch (Exception e) {
                    logger.warn("Не удалось удалить старую обложку: {}", e.getMessage());
                }
            }
            
            coverImageUrl = fileStorageService.uploadFile(coverImageFile);
            logger.info("Новая обложка загружена: {}", coverImageUrl);
        }
        
        try {
            CollectionDto collection = collectionService.updateCollection(
                id,
                title,
                description,
                isPublic,
                coverImageUrl
            );
            logger.info("=== КОЛЛЕКЦИЯ ОБНОВЛЕНА. ID: {} ===", id);
            return ResponseEntity.ok(collection);
        } catch (RuntimeException e) {
            logger.error("Ошибка обновления коллекции: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
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
            Page<CollectionDto> collections = collectionService.getCollectionsByUserId(userId, pageable);
            return ResponseEntity.ok(collections);
        } else {
            Page<CollectionDto> collections = collectionService.getPublicCollectionsByUserId(userId, pageable);
            return ResponseEntity.ok(collections);
        }
    }
    
    @Operation(summary = "Получить публичные коллекции пользователя")
    @GetMapping("/user/{userId}/public")
    public ResponseEntity<Page<CollectionDto>> getUserPublicCollections(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CollectionDto> collections = collectionService.getPublicCollectionsByUserId(userId, pageable);
        return ResponseEntity.ok(collections);
    }
    
    @Operation(summary = "Получить все публичные коллекции")
    @GetMapping("/public")
    public ResponseEntity<Page<CollectionDto>> getPublicCollections(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CollectionDto> collections = collectionService.getPublicCollections(pageable);
        return ResponseEntity.ok(collections);
    }
    
    @Operation(summary = "Поиск публичных коллекций")
    @GetMapping("/search")
    public ResponseEntity<Page<CollectionDto>> searchCollections(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CollectionDto> collections = collectionService.searchPublicCollections(q, pageable);
        return ResponseEntity.ok(collections);
    }
    
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
            String coverImageUrl = existingCollection.getCoverImageUrl();
            if (coverImageUrl != null) {
                try {
                    fileStorageService.deleteFile(coverImageUrl);
                    logger.info("Обложка коллекции удалена: {}", coverImageUrl);
                } catch (Exception e) {
                    logger.warn("Не удалось удалить обложку: {}", e.getMessage());
                }
            }
            
            collectionService.deleteCollection(id);
            logger.info("=== КОЛЛЕКЦИЯ УДАЛЕНА. ID: {} ===", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Ошибка удаления коллекции: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    private boolean isValidImageFormat(String contentType) {
        if (contentType == null) {
            return false;
        }
        
        return contentType.equals("image/jpeg") || 
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp") ||
               contentType.equals("image/svg+xml") ||
               contentType.equals("image/bmp");
    }
}