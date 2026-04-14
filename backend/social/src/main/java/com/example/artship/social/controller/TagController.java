package com.example.artship.social.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.artship.social.dto.TagDto;
import com.example.artship.social.model.Tag;
import com.example.artship.social.model.User;
import com.example.artship.social.requests.TagCreateRequest;
import com.example.artship.social.service.PermissionService;
import com.example.artship.social.service.TagService;
import com.example.artship.social.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    private static final Logger logger = LoggerFactory.getLogger(TagController.class);
    
    private final TagService tagService;
    private final UserService userService;
    private final PermissionService permissionService;
    
    public TagController(TagService tagService) {
        this.tagService = tagService;
        this.userService = new UserService();
        this.permissionService = new PermissionService();
    }
    

    // Создание тега через JSON
    @PostMapping
    @Operation(summary = "Создать тег")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Данные для создания тега",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = TagCreateRequest.class), // ← УКАЖИТЕ ПРАВИЛЬНЫЙ КЛАСС
            examples = @ExampleObject(
                name = "Пример запроса",
                value = """
                    {
                    "name": "живопись"
                    }
                    """
            )
        )
    )
    public ResponseEntity<Tag> createTag(@RequestBody TagCreateRequest request) {
        try {
            Tag tag = tagService.createTag(request.getName());
            return new ResponseEntity<>(tag, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Получение всех тегов
    @GetMapping
    public ResponseEntity<List<TagDto>> getAllTags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        List<TagDto> tags = tagService.getAllTagDtos();
        return ResponseEntity.ok(tags);
    }
    
    // Получение тега по ID
    @GetMapping("/{id}")
    public ResponseEntity<TagDto> getTagById(@PathVariable Long id) {
        Optional<TagDto> tag = tagService.getTagDtoById(id);
        return tag.map(ResponseEntity::ok)
                 .orElse(ResponseEntity.notFound().build());
    }
    
    // Получение тега по имени
    @GetMapping("/name/{name}")
    public ResponseEntity<TagDto> getTagByName(@PathVariable String name) {
        Optional<TagDto> tag = tagService.getTagDtoByName(name);
        return tag.map(ResponseEntity::ok)
                 .orElse(ResponseEntity.notFound().build());
    }
    
    // Популярные теги
    @GetMapping("/popular")
    public ResponseEntity<List<TagDto>> getPopularTags(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<TagDto> tags = tagService.getPopularTagDtos();
        if (tags.size() > limit) {
            tags = tags.subList(0, limit);
        }
        return ResponseEntity.ok(tags);
    }
    
    // Теги для конкретного арта
    @GetMapping("/art/{artId}")
    public ResponseEntity<List<TagDto>> getTagsByArtId(@PathVariable Long artId) {
        List<TagDto> tags = tagService.getTagDtosByArtId(artId);
        return ResponseEntity.ok(tags);
    }
    
    // Обновление тега
   @Operation(summary = "Обновить тег (только для модераторов и администраторов)")
    @PutMapping("/{id}")
    public ResponseEntity<Tag> updateTag(
            @PathVariable Long id,
            @RequestParam String name,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        
        // Проверка авторизации
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Получение пользователя
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        
        if (!permissionService.isModerator(currentUser) && !permissionService.isAdmin(currentUser)) {
            logger.warn("Пользователь {} не имеет прав на обновление тега. Роль: {}", 
                    currentUser.getUsername(), currentUser.getUserRole());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Tag updatedTag = tagService.updateTag(id, name);
            return ResponseEntity.ok(updatedTag);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Обновление тега через JSON
    @Operation(summary = "Обновить тег через JSON (только для модераторов и администраторов)")
    @PutMapping("/{id}/update")
    public ResponseEntity<Tag> updateTagJson(
            @PathVariable Long id,
            @RequestBody Tag tagRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("=== ОБНОВЛЕНИЕ ТЕГА ID: {} через JSON ===", id);
        
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
        
        if (!permissionService.isModerator(currentUser) && !permissionService.isAdmin(currentUser)) {
            logger.warn("Пользователь {} не имеет прав на обновление тега. Роль: {}", 
                    currentUser.getUsername(), currentUser.getUserRole());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        if (tagRequest.getName() == null || tagRequest.getName().trim().isEmpty()) {
            logger.error("Имя тега не может быть пустым");
            return ResponseEntity.badRequest().build();
        }
        
        logger.info("{} {} обновляет тег ID: {} на имя: {}", 
                currentUser.getUserRole(), currentUser.getUsername(), id, tagRequest.getName());
        
        try {
            Tag updatedTag = tagService.updateTag(id, tagRequest.getName());
            return ResponseEntity.ok(updatedTag);
        } catch (RuntimeException e) {
            logger.error("Ошибка обновления тега: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Удаление тега
        @Operation(summary = "Удалить тег (только для модераторов и администраторов)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("=== УДАЛЕНИЕ ТЕГА ID: {} ===", id);
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        
        if (!permissionService.isModerator(currentUser) && !permissionService.isAdmin(currentUser)) {
            logger.warn("Пользователь {} не имеет прав на удаление тега. Роль: {}", 
                    currentUser.getUsername(), currentUser.getUserRole());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        logger.info("{} {} удаляет тег ID: {}", 
                currentUser.getUserRole(), currentUser.getUsername(), id);
        
        try {
            tagService.deleteTag(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Ошибка удаления тега: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    // Создание нескольких тегов
    @PostMapping("/batch")
    public ResponseEntity<List<Tag>> createTags(@RequestBody List<String> tagNames) {
        List<Tag> tags = tagService.createTags(tagNames);
        return new ResponseEntity<>(tags, HttpStatus.CREATED);
    }
    
    // Автодополнение тегов
    @GetMapping("/autocomplete")
    public ResponseEntity<List<TagDto>> autocompleteTags(@RequestParam String q) {
        List<TagDto> tags = tagService.searchTagDtos(q);
        return ResponseEntity.ok(tags);
    }
    
    // Проверка существования тега
    @GetMapping("/exists/{name}")
    public ResponseEntity<Boolean> tagExists(@PathVariable String name) {
        boolean exists = tagService.tagExists(name);
        return ResponseEntity.ok(exists);
    }
    
    // Количество артов по тегу
    @GetMapping("/{id}/art-count")
    public ResponseEntity<Long> getArtCountByTag(@PathVariable Long id) {
        Long artCount = tagService.getArtCountByTagId(id);
        return ResponseEntity.ok(artCount);
    }
}