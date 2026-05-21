package com.example.artship.social.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.artship.social.dto.ArtDto;
import com.example.artship.social.model.Art;
import com.example.artship.social.model.User;
import com.example.artship.social.model.enumclass.ArtStatus;
import com.example.artship.social.requests.CreateArtRequest;
import com.example.artship.social.requests.PrivacyUpdateRequest;
import com.example.artship.social.requests.UpdateArtRequest;
import com.example.artship.social.service.ArtService;
import com.example.artship.social.service.LocalFileStorageService;
import com.example.artship.social.service.PermissionService;
import com.example.artship.social.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/arts")
@Tag(name = "Art Controller", description = "API для управления артами")
public class ArtController {
    
    private static final Logger logger = LoggerFactory.getLogger(ArtController.class);
    
    private final ArtService artService;
    private final UserService userService;
    private final LocalFileStorageService fileStorageService;
    private final PermissionService permissionService;
    
    public ArtController(ArtService artService, 
                         UserService userService, 
                         LocalFileStorageService fileStorageService,
                         PermissionService permissionService) {
        this.artService = artService;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
        this.permissionService = permissionService;
    }
    
    
    @Operation(
        summary = "Создать новый арт",
        description = "Создает арт с загрузкой изображения. Формат запроса: multipart/form-data"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Арт успешно создан"),
        @ApiResponse(responseCode = "400", description = "Неверные данные запроса"),
        @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, 
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArtDto> createArt(
            @Parameter(description = "Данные арта и файл изображения", required = true)
            @ModelAttribute CreateArtRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
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
        
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            logger.error("Заголовок не указан");
            return ResponseEntity.badRequest().build();
        }
        
        if (request.getImageFile() == null || request.getImageFile().isEmpty()) {
            logger.error("Файл изображения не загружен");
            return ResponseEntity.badRequest().build();
        }
        
        String contentType = request.getImageFile().getContentType();
        if (!isValidImageFormat(contentType)) {
            logger.error("Неподдерживаемый формат: {}", contentType);
            return ResponseEntity.badRequest().build();
        }
        
        long fileSize = request.getImageFile().getSize();
        if (fileSize > 10 * 1024 * 1024) {
            logger.error("Файл слишком большой: {} байт", fileSize);
            return ResponseEntity.badRequest().build();
        }
        
        String imageUrl = fileStorageService.uploadFile(request.getImageFile());
        logger.info("Файл загружен: {}", imageUrl);
        
        Art art = new Art();
        art.setTitle(request.getTitle().trim());
        art.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        art.setImageUrl(imageUrl);
        art.setProjectDataUrl(request.getProjectDataUrl() != null ? request.getProjectDataUrl().trim() : null);
        art.setIsPublicFlag(request.getIsPublicFlag() != null ? request.getIsPublicFlag() : true);
        
        ArtDto createdArt = artService.createArt(art, currentUser.getId());
        logger.info("=== АРТ СОЗДАН. ID: {} ===", createdArt.getId());
        
        return new ResponseEntity<>(createdArt, HttpStatus.CREATED);
    }
        
    @Operation(summary = "Обновить арт")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Арт успешно обновлен"),
        @ApiResponse(responseCode = "403", description = "Нет прав на редактирование"),
        @ApiResponse(responseCode = "404", description = "Арт не найден"),
        @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
               produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArtDto> updateArt(
            @Parameter(description = "ID арта", required = true) 
            @PathVariable Long id,
            @Parameter(description = "Обновленные данные арта", required = true)
            @ModelAttribute UpdateArtRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("=== ОБНОВЛЕНИЕ АРТА ID: {} ===", id);
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        
        Optional<Art> artOpt = artService.getArtById(id);
        if (artOpt.isEmpty()) {
            logger.warn("Арт с ID {} не найден", id);
            return ResponseEntity.notFound().build();
        }
        
        Art existingArt = artOpt.get();
        
        if (!permissionService.canEditContent(currentUser, existingArt)) {
            logger.warn("Пользователь {} не имеет прав на редактирование арта {}", 
                       currentUser.getUsername(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            existingArt.setTitle(request.getTitle().trim());
            logger.info("Обновлен заголовок: {}", existingArt.getTitle());
        }
        
        if (request.getDescription() != null) {
            existingArt.setDescription(request.getDescription().trim());
            logger.info("Обновлено описание");
        }
        
        if (request.getProjectDataUrl() != null) {
            existingArt.setProjectDataUrl(request.getProjectDataUrl().trim());
            logger.info("Обновлен URL проекта");
        }
        
        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            String contentType = request.getImageFile().getContentType();
            if (!isValidImageFormat(contentType)) {
                return ResponseEntity.badRequest().build();
            }
            
            long fileSize = request.getImageFile().getSize();
            if (fileSize > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().build();
            }
            
            String newImageUrl = fileStorageService.uploadFile(request.getImageFile());
            logger.info("Новое изображение загружено: {}", newImageUrl);
            
            try {
                fileStorageService.deleteFile(existingArt.getImageUrl());
                logger.info("Старое изображение удалено: {}", existingArt.getImageUrl());
            } catch (Exception e) {
                logger.warn("Не удалось удалить старое изображение: {}", e.getMessage());
            }
            
            existingArt.setImageUrl(newImageUrl);
        }
        
        existingArt.setUpdatedAt(LocalDateTime.now());
        
        ArtDto updatedArt = artService.updateArt(id, existingArt);
        logger.info("=== АРТ ОБНОВЛЕН. ID: {} ===", id);
        
        return ResponseEntity.ok(updatedArt);
    }
    
    @Operation(summary = "Изменение приватности арта")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Приватность арта успешно изменена",
                content = @Content(schema = @Schema(implementation = ArtDto.class))),
        @ApiResponse(responseCode = "404", description = "Арт не найден"),
        @ApiResponse(responseCode = "403", description = "Нет прав на изменение этого арта"),
        @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @PatchMapping(value = "/{id}/privacy", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArtDto> updateArtPrivacy(
            @Parameter(description = "ID арта", required = true) 
            @PathVariable Long id,
            @Parameter(
                description = "Данные для изменения приватности",
                required = true,
                content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = PrivacyUpdateRequest.class)
                )
            )
            @ModelAttribute PrivacyUpdateRequest privacyRequest,
            @AuthenticationPrincipal UserDetails userDetails) {  
        
        logger.info("=== ИЗМЕНЕНИЕ ПРИВАТНОСТИ АРТА ID: {} ===", id);
        
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
        
        if (privacyRequest.getIsPublicFlag() == null) {
            logger.error("isPublicFlag не может быть null");
            return ResponseEntity.badRequest().build();
        }
        
        Optional<Art> artOpt = artService.getArtById(id);
        if (artOpt.isEmpty()) {
            logger.warn("Арт с ID {} не найден", id);
            return ResponseEntity.notFound().build();
        }
        
        Art art = artOpt.get();
        
        if (!permissionService.canManageArt(currentUser, art)) {
            logger.warn("Пользователь {} не имеет прав на изменение приватности арта {}", 
                    currentUser.getUsername(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        art.setIsPublicFlag(privacyRequest.getIsPublicFlag());
        art.setUpdatedAt(LocalDateTime.now());
        
        Art updatedArt = artService.save(art);
        ArtDto result = artService.convertToDto(updatedArt);
        
        logger.info("Приватность арта {} изменена на {} пользователем {}", 
                id, privacyRequest.getIsPublicFlag(), currentUser.getUsername());
        
        return ResponseEntity.ok(result);
    }
    
    
    @Operation(summary = "Удалить арт")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Арт успешно удален"),
        @ApiResponse(responseCode = "403", description = "Нет прав для удаления"),
        @ApiResponse(responseCode = "404", description = "Арт не найден"),
        @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArt(
            @Parameter(description = "ID арта", required = true) 
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("=== УДАЛЕНИЕ АРТА ID: {} ===", id);
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        
        Optional<Art> artOpt = artService.getArtById(id);
        if (artOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Art art = artOpt.get();
        
        if (!permissionService.canManageArt(currentUser, art)) {
            logger.warn("Пользователь {} не имеет прав на удаление арта {}", 
                       currentUser.getUsername(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            fileStorageService.deleteFile(art.getImageUrl());
            logger.info("Файл изображения удален: {}", art.getImageUrl());
        } catch (Exception e) {
            logger.warn("Не удалось удалить файл изображения: {}", e.getMessage());
        }
        
        artService.deleteArt(id);
        logger.info("=== АРТ УДАЛЕН. ID: {} пользователем: {} ===", id, currentUser.getUsername());
        
        return ResponseEntity.noContent().build();
    }

    
    @Operation(summary = "Получить арт по ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Арт найден"),
        @ApiResponse(responseCode = "403", description = "Нет доступа к приватному арту"),
        @ApiResponse(responseCode = "404", description = "Арт не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ArtDto> getArtById(
            @Parameter(description = "ID арта", required = true) 
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("Получение арта по ID: {}", id);
        
        Optional<Art> artOpt = artService.getArtById(id);
        if (artOpt.isEmpty()) {
            logger.warn("Арт с ID {} не найден", id);
            return ResponseEntity.notFound().build();
        }
        
        Art art = artOpt.get();
        
        User currentUser = null;
        if (userDetails != null) {
            Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
            currentUser = userOpt.orElse(null);
        }
        
        if (!permissionService.canViewArt(currentUser, art)) {
            logger.warn("Пользователь {} не имеет доступа к арту {}", 
                       currentUser != null ? currentUser.getUsername() : "неавторизованный", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        ArtDto artDto = artService.convertToDto(art);
        return ResponseEntity.ok(artDto);
    }
    
    @Operation(summary = "Получить публичные арты с пагинацией")
    @GetMapping("/public")
    public ResponseEntity<Page<ArtDto>> getPublicArts(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество элементов на странице", example = "20") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Поле для сортировки", example = "createdAt") 
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Направление сортировки", example = "desc") 
            @RequestParam(defaultValue = "desc") String direction,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("Получение публичных артов. Страница: {}, Размер: {}", page, size);
        
        Sort sort = direction.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Получаем текущего пользователя (может быть null)
        User currentUser = null;
        if (userDetails != null) {
            Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
            currentUser = userOpt.orElse(null);
        }
        
        Page<ArtDto> arts = artService.getPublicArtsDtos(pageable, currentUser);
        
        logger.info("Найдено {} публичных артов", arts.getTotalElements());
        return ResponseEntity.ok(arts);
    }
    
    @Operation(summary = "Получить ленту пользователя")
    @GetMapping("/feed")
    public ResponseEntity<Page<ArtDto>> getUserFeed(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество элементов на странице", example = "20") 
            @RequestParam(defaultValue = "20") int size) {
        
        logger.info("Получение ленты пользователя");
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<ArtDto> feed = artService.getUserFeedDtos(currentUser.getId(), pageable, currentUser);
        
        logger.info("Лента пользователя {} содержит {} артов", 
                   currentUser.getUsername(), feed.getTotalElements());
        return ResponseEntity.ok(feed);
    }
    
    @Operation(summary = "Получить публичные арты пользователя")
    @GetMapping("/author/{userId}")
    public ResponseEntity<Page<ArtDto>> getPublicArtsByAuthor(
            @Parameter(description = "ID пользователя", required = true) 
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("Получение публичных артов пользователя ID: {}", userId);
        
        Optional<User> author = userService.findById(userId);
        if (author.isEmpty()) {
            logger.warn("Пользователь с ID {} не найден", userId);
            return ResponseEntity.notFound().build();
        }
        
        // Получаем текущего пользователя для проверки прав
        User currentUser = null;
        if (userDetails != null) {
            Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
            currentUser = userOpt.orElse(null);
        }
        
        Page<ArtDto> arts = artService.getPublicArtDtosByAuthor(author.get(), pageable, currentUser);
        
        logger.info("Найдено {} публичных артов пользователя {}", 
                arts.getTotalElements(), author.get().getUsername());
        
        return ResponseEntity.ok(arts);
    }
    
    @Operation(summary = "Получить все арты текущего пользователя")
    @GetMapping("/my-arts")
    public ResponseEntity<Page<ArtDto>> getMyArts(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        
        logger.info("Получение всех артов текущего пользователя");
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        
        Page<ArtDto> arts = artService.getAllArtDtosByAuthor(currentUser, pageable, currentUser);
        
        logger.info("Найдено {} артов пользователя {}", 
                arts.getTotalElements(), currentUser.getUsername());
        
        return ResponseEntity.ok(arts);
    }

    
    @Operation(summary = "Проверить доступ к арту")
    @GetMapping("/{id}/access")
    public ResponseEntity<Boolean> checkArtAccess(
            @Parameter(description = "ID арта", required = true) 
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("Проверка доступа к арту ID: {}", id);
        
        Optional<Art> artOpt = artService.getArtById(id);
        if (artOpt.isEmpty()) {
            logger.warn("Арт с ID {} не найден", id);
            return ResponseEntity.notFound().build();
        }
        
        Art art = artOpt.get();
        
        User currentUser = null;
        if (userDetails != null) {
            Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
            currentUser = userOpt.orElse(null);
        }
        
        boolean hasAccess = permissionService.canViewArt(currentUser, art);
        
        logger.info("Доступ к арту {}: {}", id, hasAccess);
        return ResponseEntity.ok(hasAccess);
    }



    /**
     * Скрыть арт (доступно модераторам и администраторам)
     * Статус арта становится HIDDEN, арт не отображается обычным пользователям
     */
    @Operation(summary = "Скрыть арт", 
               description = "Скрывает арт от обычных пользователей. Доступно модераторам и администраторам.")
    @PatchMapping("/{artId}/hide")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<ArtDto> hideArt(
            @Parameter(description = "ID арта", required = true) 
            @PathVariable Long artId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("=== СКРЫТИЕ АРТА ID: {} ===", artId);
        
        // Проверка авторизации
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            artService.hideArt(artId);
            logger.info("Арт {} скрыт пользователем {}", artId, userDetails.getUsername());
            
            // Возвращаем обновленный арт
            Optional<ArtDto> art = artService.getArtDtoById(artId, null);
            return art.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
            
        } catch (RuntimeException e) {
            logger.error("Ошибка при скрытии арта: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Восстановить арт (доступно модераторам и администраторам)
     * Статус арта становится ACTIVE, арт снова отображается
     */
    @Operation(summary = "Восстановить арт", 
               description = "Восстанавливает скрытый арт. Доступно модераторам и администраторам.")
    @PatchMapping("/{artId}/unhide")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<ArtDto> unhideArt(
            @Parameter(description = "ID арта", required = true) 
            @PathVariable Long artId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("=== ВОССТАНОВЛЕНИЕ АРТА ID: {} ===", artId);
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            artService.unhideArt(artId);
            logger.info("Арт {} восстановлен пользователем {}", artId, userDetails.getUsername());
            
            Optional<ArtDto> art = artService.getArtDtoById(artId, null);
            return art.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
            
        } catch (RuntimeException e) {
            logger.error("Ошибка при восстановлении арта: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Забанить арт (только для администратора)
     * Статус арта становится BANNED, арт полностью недоступен
     */
    @Operation(summary = "Забанить арт", 
               description = "Полностью блокирует арт. Доступно только администраторам.")
    @PatchMapping("/{artId}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtDto> banArt(
            @Parameter(description = "ID арта", required = true) 
            @PathVariable Long artId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("=== БАЛОКИРОВКА АРТА ID: {} ===", artId);
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            artService.banArt(artId);
            logger.info("Арт {} забанен администратором {}", artId, userDetails.getUsername());
            
            Optional<ArtDto> art = artService.getArtDtoById(artId, null);
            return art.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
            
        } catch (RuntimeException e) {
            logger.error("Ошибка при бане арта: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Полностью удалить арт (только для администратора)
     * Арт полностью удаляется из системы вместе со всеми связями
     */
    @Operation(summary = "Полностью удалить арт", 
               description = "Полностью удаляет арт из системы. Доступно только администраторам.")
    @DeleteMapping("/{artId}/force")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> forceDeleteArt(
            @Parameter(description = "ID арта", required = true) 
            @PathVariable Long artId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("=== ПРИНУДИТЕЛЬНОЕ УДАЛЕНИЕ АРТА ID: {} ===", artId);
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            artService.forceDeleteArt(artId);
            logger.info("Арт {} принудительно удалён администратором {}", 
                       artId, userDetails.getUsername());
            return ResponseEntity.noContent().build();
            
        } catch (RuntimeException e) {
            logger.error("Ошибка при удалении арта: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Получить арты по статусу (для админ/модератор панели)
     */
    @Operation(summary = "Получить арты по статусу", 
               description = "Возвращает список артов с фильтрацией по статусу. Доступно модераторам и администраторам.")
    @GetMapping("/admin/by-status")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<Page<ArtDto>> getArtsByStatus(
            @Parameter(description = "Статус арта (ACTIVE, HIDDEN, BANNED, DELETED_BY_USER)")
            @RequestParam(required = false) ArtStatus status,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Сортировка") @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        logger.info("Получение артов по статусу: {}", status);
        
        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        
        Page<ArtDto> arts = artService.getArtsByStatus(status, pageable);
        
        logger.info("Найдено {} артов", arts.getTotalElements());
        return ResponseEntity.ok(arts);
    }
    
    /**
     * Получить статистику по статусам артов (только для администратора)
     */
    @Operation(summary = "Статистика по статусам артов", 
               description = "Возвращает количество артов в каждом статусе. Доступно только администраторам.")
    @GetMapping("/admin/status-statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<ArtStatus, Long>> getArtsStatusStatistics() {
        logger.info("Получение статистики по статусам артов");
        Map<ArtStatus, Long> statistics = artService.getArtsStatusStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    // Вспомогательный метод для парсинга сортировки
    private Sort parseSort(String sortParam) {
        String[] sortParts = sortParam.split(",");
        if (sortParts.length == 2) {
            String field = sortParts[0];
            String direction = sortParts[1];
            return "desc".equalsIgnoreCase(direction) 
                ? Sort.by(field).descending() 
                : Sort.by(field).ascending();
        }
        return Sort.by("createdAt").descending();
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