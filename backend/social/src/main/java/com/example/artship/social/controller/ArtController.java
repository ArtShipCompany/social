package com.example.artship.social.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.artship.social.dto.ArtDto;
import com.example.artship.social.dto.CreateArtRequest;
import com.example.artship.social.dto.UpdateArtRequest;
import com.example.artship.social.model.Art;
import com.example.artship.social.model.User;
import com.example.artship.social.service.ArtService;
import com.example.artship.social.service.LocalFileStorageService;
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
    
    public ArtController(ArtService artService, UserService userService, 
                        LocalFileStorageService fileStorageService) {
        this.artService = artService;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }


    

    

    @Operation(
        summary = "Создать новый арт",
        description = "Создает арт с загрузкой изображения. Формат запроса: multipart/form-data"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Арт успешно создан",
                    content = @Content(schema = @Schema(implementation = ArtDto.class))),
        @ApiResponse(responseCode = "400", description = "Неверные данные запроса"),
        @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, 
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArtDto> createArt(
        @Parameter(
            description = "Данные арта и файл изображения",
            required = true,
            content = @Content(
                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                schema = @Schema(implementation = CreateArtRequest.class)
            )
        )
        @ModelAttribute CreateArtRequest request,
        @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        logger.info("=== НАЧАЛО СОЗДАНИЯ АРТА ===");
        logger.info("Тип userDetails: {}", userDetails != null ? userDetails.getClass().getName() : "null");
        logger.info("userDetails: {}", userDetails);
        logger.info("Пользователь: {}", userDetails != null ? userDetails.getUsername() : "null");
        logger.info("Запрос получен. Title: {}, Файл: {}", 
                   request.getTitle(), 
                   request.getImageFile() != null ? request.getImageFile().getOriginalFilename() : "null");
        
        try {
            // Валидация обязательных полей
            if (userDetails == null) {
                logger.error("ОШИБКА: userDetails == null. Возвращаем 401");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Находим пользователя в базе
            Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
            if (userOpt.isEmpty()) {
                logger.error("ОШИБКА: Пользователь не найден в базе: {}", userDetails.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            User currentUser = userOpt.get();
            logger.info("Пользователь найден в базе. ID: {}, Username: {}", 
                       currentUser.getId(), currentUser.getUsername());
            
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                logger.error("ОШИБКА: Заголовок не указан");
                return ResponseEntity.badRequest().body(null);
            }
            
            if (request.getImageFile() == null || request.getImageFile().isEmpty()) {
                logger.error("ОШИБКА: Файл изображения не загружен");
                return ResponseEntity.badRequest().body(null);
            }
            
            // Валидация формата изображения
            String contentType = request.getImageFile().getContentType();
            logger.info("Content-Type файла: {}", contentType);
            
            if (!isValidImageFormat(contentType)) {
                logger.error("ОШИБКА: Неподдерживаемый формат изображения: {}", contentType);
                return ResponseEntity.badRequest().body(null);
            }
            
            // Валидация размера файла (максимум 10MB)
            long fileSize = request.getImageFile().getSize();
            logger.info("Размер файла: {} байт", fileSize);
            
            if (fileSize > 10 * 1024 * 1024) {
                logger.error("ОШИБКА: Размер файла превышает 10MB: {} байт", fileSize);
                return ResponseEntity.badRequest().body(null);
            }
            
            // Загружаем изображение
            String imageUrl;
            try {
                logger.info("Начинаю загрузку файла...");
                imageUrl = fileStorageService.uploadFile(request.getImageFile());
                logger.info("Файл загружен. URL: {}", imageUrl);
            } catch (Exception e) {
                logger.error("ОШИБКА при загрузке файла: {}", e.getMessage(), e);
                return ResponseEntity.badRequest().body(null);
            }
            
            // Создаем объект Art
            Art art = new Art();
            art.setTitle(request.getTitle().trim());
            art.setDescription(request.getDescription() != null ? 
                             request.getDescription().trim() : null);
            art.setImageUrl(imageUrl); // Используем полученный URL
            art.setProjectDataUrl(request.getProjectDataUrl() != null ? 
                                request.getProjectDataUrl().trim() : null);
            art.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : true);
            
            logger.info("Создаю арт. Автор ID: {}, Заголовок: {}, Изображение: {}", 
                       currentUser.getId(), art.getTitle(), art.getImageUrl());
            
            ArtDto createdArt = artService.createArt(art, currentUser.getId());
            logger.info("=== АРТ УСПЕШНО СОЗДАН. ID: {} ===", createdArt.getId());
            
            return new ResponseEntity<>(createdArt, HttpStatus.CREATED);
            
        } catch (Exception e) {
            logger.error("НЕОБРАБОТАННАЯ ОШИБКА при создании арта: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Обновление арта с возможностью загрузки нового изображения
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
           produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArtDto> updateArt(
        @Parameter(description = "ID арта", required = true) @PathVariable Long id,
        @Parameter(
            description = "Обновленные данные арта",
            required = true,
            content = @Content(
                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                schema = @Schema(implementation = UpdateArtRequest.class)
            )
        )
        @ModelAttribute UpdateArtRequest request,
        @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        logger.info("=== НАЧАЛО ОБНОВЛЕНИЯ АРТА ID: {} ===", id);
        logger.info("Пользователь: {}", userDetails != null ? userDetails.getUsername() : "null");
        logger.info("Данные запроса - title: {}, description: {}, hasImage: {}", 
                request.getTitle(), 
                request.getDescription(),
                request.getImageFile() != null && !request.getImageFile().isEmpty());
        
        try {
            if (userDetails == null) {
                logger.error("ОШИБКА: userDetails == null. Возвращаем 401");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            

            Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
            if (userOpt.isEmpty()) {
                logger.error("ОШИБКА: Пользователь не найден в базе: {}", userDetails.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            User currentUser = userOpt.get();

            if (!artService.isUserAuthorOfArt(id, currentUser.getId())) {
                logger.error("ОШИБКА: Пользователь {} не является автором арта {}", 
                        currentUser.getId(), id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
         
            Optional<Art> existingArtOpt = artService.getArtById(id);
            if (existingArtOpt.isEmpty()) {
                logger.error("ОШИБКА: Арт с ID {} не найден", id);
                return ResponseEntity.notFound().build();
            }
            
            Art existingArt = existingArtOpt.get();
            logger.info("Найден арт: {}, Автор: {}", existingArt.getTitle(), existingArt.getAuthor().getId());
            
           
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
                logger.info("Обновлен URL проекта: {}", existingArt.getProjectDataUrl());
            }
            
            if (request.getIsPublic() != null) {
                existingArt.setIsPublic(request.getIsPublic());
                logger.info("Обновлена публичность: {}", existingArt.getIsPublic());
            }
            
            // Обновление изображения, если загружено новое
            if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
                logger.info("Загружено новое изображение: {}", 
                        request.getImageFile().getOriginalFilename());
                
                String contentType = request.getImageFile().getContentType();
                if (!isValidImageFormat(contentType)) {
                    logger.error("ОШИБКА: Неподдерживаемый формат изображения: {}", contentType);
                    return ResponseEntity.badRequest().body(null);
                }

                long fileSize = request.getImageFile().getSize();
                if (fileSize > 10 * 1024 * 1024) {
                    logger.error("ОШИБКА: Размер файла превышает 10MB: {} байт", fileSize);
                    return ResponseEntity.badRequest().body(null);
                }
                
                String newImageUrl = fileStorageService.uploadFile(request.getImageFile());
                logger.info("Новое изображение загружено. URL: {}", newImageUrl);
                
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
            logger.info("=== АРТ УСПЕШНО ОБНОВЛЕН. ID: {} ===", id);
            
            return ResponseEntity.ok(updatedArt);
            
        } catch (Exception e) {
            logger.error("НЕОБРАБОТАННАЯ ОШИБКА при обновлении арта: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    
    // Получение арта по ID
    @Operation(summary = "Получить арт по ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Арт найден",
                    content = @Content(schema = @Schema(implementation = ArtDto.class))),
        @ApiResponse(responseCode = "404", description = "Арт не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ArtDto> getArtById(
        @Parameter(description = "ID арта", required = true) @PathVariable Long id
    ) {
        logger.info("Получение арта по ID: {}", id);
        
        Optional<ArtDto> art = artService.getArtDtoById(id);
        if (art.isPresent()) {
            logger.info("Арт найден: {}", art.get().getTitle());
            return ResponseEntity.ok(art.get());
        } else {
            logger.warn("Арт с ID {} не найден", id);
            return ResponseEntity.notFound().build();
        }
    }

    // Удаление арта
    @Operation(summary = "Удалить арт")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Арт успешно удален"),
        @ApiResponse(responseCode = "403", description = "Нет прав для удаления"),
        @ApiResponse(responseCode = "404", description = "Арт не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArt(
        @Parameter(description = "ID арта", required = true) @PathVariable Long id,
        @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        logger.info("=== УДАЛЕНИЕ АРТА ID: {} ===", id);
        logger.info("Пользователь: {}", userDetails != null ? userDetails.getUsername() : "null");
        
        if (userDetails == null) {
            logger.error("ОШИБКА: userDetails == null. Возвращаем 401");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            logger.error("ОШИБКА: Пользователь не найден в базе: {}", userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        
        if (!artService.isUserAuthorOfArt(id, currentUser.getId())) {
            logger.error("ОШИБКА: Пользователь {} не является автором арта {}", 
                       currentUser.getId(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Art> art = artService.getArtById(id);
        if (art.isPresent()) {
            try {
                fileStorageService.deleteFile(art.get().getImageUrl());
                logger.info("Файл изображения удален: {}", art.get().getImageUrl());
            } catch (Exception e) {
                logger.warn("Не удалось удалить файл изображения: {}", e.getMessage());
            }
        }
        
        artService.deleteArt(id);
        logger.info("=== АРТ УСПЕШНО УДАЛЕН. ID: {} ===", id);
        
        return ResponseEntity.noContent().build();
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
        @RequestParam(defaultValue = "desc") String direction
    ) {
        logger.info("Получение публичных артов. Страница: {}, Размер: {}", page, size);
        
        Sort sort = direction.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ArtDto> arts = artService.getPublicArtsDtos(pageable);
        
        logger.info("Найдено {} публичных артов", arts.getTotalElements());
        return ResponseEntity.ok(arts);
    }

    // Лента пользователя (арты тех, на кого подписан)
    @Operation(summary = "Получить ленту пользователя")
    @GetMapping("/feed")
    public ResponseEntity<Page<ArtDto>> getUserFeed(
        @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
        @Parameter(description = "Номер страницы (начиная с 0)", example = "0") 
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Количество элементов на странице", example = "20") 
        @RequestParam(defaultValue = "20") int size
    ) {
        logger.info("Получение ленты пользователя");
        
        if (userDetails == null) {
            logger.error("ОШИБКА: userDetails == null. Возвращаем 401");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            logger.error("ОШИБКА: Пользователь не найден в базе: {}", userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ArtDto> feed = artService.getUserFeedDtos(currentUser.getId(), pageable);
        
        logger.info("Лента пользователя {} содержит {} артов", 
                   currentUser.getUsername(), feed.getTotalElements());
        return ResponseEntity.ok(feed);
    }

    // Получение публичных артов конкретного пользователя
    @Operation(summary = "Получить публичные арты пользователя")
    @GetMapping("/author/{userId}")
    public ResponseEntity<List<ArtDto>> getPublicArtsByAuthor(
        @Parameter(description = "ID пользователя", required = true) @PathVariable Long userId
    ) {
        logger.info("Получение публичных артов пользователя ID: {}", userId);
        
        Optional<User> author = userService.findById(userId);
        if (author.isEmpty()) {
            logger.warn("Пользователь с ID {} не найден", userId);
            return ResponseEntity.notFound().build();
        }
        
        List<ArtDto> arts = artService.getPublicArtDtosByAuthor(author.get());
        logger.info("Найдено {} публичных артов пользователя {}", 
                   arts.size(), author.get().getUsername());
        
        return ResponseEntity.ok(arts);
    }

    // Получение всех артов текущего пользователя (включая приватные)
    @Operation(summary = "Получить все арты текущего пользователя")
    @GetMapping("/my-arts")
    public ResponseEntity<List<ArtDto>> getMyArts(
        @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        logger.info("Получение всех артов текущего пользователя");
        
        if (userDetails == null) {
            logger.error("ОШИБКА: userDetails == null. Возвращаем 401");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Находим пользователя в базе
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            logger.error("ОШИБКА: Пользователь не найден в базе: {}", userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        
        List<ArtDto> arts = artService.getAllArtDtosByAuthor(currentUser);
        logger.info("Найдено {} артов пользователя {}", 
                   arts.size(), currentUser.getUsername());
        
        return ResponseEntity.ok(arts);
    }

    // Поиск публичных артов по названию
    @Operation(summary = "Поиск публичных артов по названию")
    @GetMapping("/search")
    public ResponseEntity<Page<ArtDto>> searchPublicArts(
        @Parameter(description = "Название для поиска", required = true) 
        @RequestParam String title,
        @Parameter(description = "Номер страницы (начиная с 0)", example = "0") 
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Количество элементов на странице", example = "20") 
        @RequestParam(defaultValue = "20") int size
    ) {
        logger.info("Поиск публичных артов по названию: '{}'", title);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ArtDto> arts = artService.searchPublicArtDtosByTitle(title, pageable);
        
        logger.info("Найдено {} артов по запросу '{}'", arts.getTotalElements(), title);
        return ResponseEntity.ok(arts);
    }

    // Поиск публичных артов по тегу
    @Operation(summary = "Получить арты по тегу")
    @GetMapping("/tag/{tagName}")
    public ResponseEntity<Page<ArtDto>> getArtsByTag(
        @Parameter(description = "Название тега", required = true) @PathVariable String tagName,
        @Parameter(description = "Номер страницы (начиная с 0)", example = "0") 
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Количество элементов на странице", example = "20") 
        @RequestParam(defaultValue = "20") int size
    ) {
        logger.info("Поиск артов по тегу: '{}'", tagName);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ArtDto> arts = artService.findDtosByTagName(tagName, pageable);
        
        logger.info("Найдено {} артов с тегом '{}'", arts.getTotalElements(), tagName);
        return ResponseEntity.ok(arts);
    }

    // Проверка прав доступа к арту
    @Operation(summary = "Проверить доступ к арту")
    @GetMapping("/{id}/access")
    public ResponseEntity<Boolean> checkArtAccess(
        @Parameter(description = "ID арта", required = true) @PathVariable Long id,
        @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        logger.info("Проверка доступа к арту ID: {}", id);
        
        if (userDetails == null) {
            logger.info("Пользователь не аутентифицирован. Доступ: false");
            return ResponseEntity.ok(false);
        }
        
        Optional<Art> art = artService.getArtById(id);
        if (art.isEmpty()) {
            logger.warn("Арт с ID {} не найден", id);
            return ResponseEntity.notFound().build();
        }
        
        // Находим пользователя в базе
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            logger.warn("Пользователь не найден в базе: {}", userDetails.getUsername());
            return ResponseEntity.ok(false);
        }
        
        User currentUser = userOpt.get();
        
        Art artEntity = art.get();
        boolean hasAccess = artEntity.getIsPublic() || 
                           artEntity.getAuthor().getId().equals(currentUser.getId());
        
        logger.info("Доступ к арту {}: {}", id, hasAccess);
        return ResponseEntity.ok(hasAccess);
    }

    // Метод для валидации формата изображения
    private boolean isValidImageFormat(String contentType) {
        if (contentType == null) {
            logger.warn("Content-Type is null");
            return false;
        }
        
        boolean isValid = contentType.equals("image/jpeg") || 
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp") ||
               contentType.equals("image/svg+xml") ||
               contentType.equals("image/bmp");
        
        if (!isValid) {
            logger.warn("Неподдерживаемый Content-Type: {}", contentType);
        }
        
        return isValid;
    }
}