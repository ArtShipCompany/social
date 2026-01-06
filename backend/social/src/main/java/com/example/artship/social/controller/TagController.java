package com.example.artship.social.controller;

import com.example.artship.social.dto.TagCreateRequest;
import com.example.artship.social.dto.TagDto;
import com.example.artship.social.model.Tag;
import com.example.artship.social.service.TagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    
    private final TagService tagService;
    
    public TagController(TagService tagService) {
        this.tagService = tagService;
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
    
    // Поиск тегов по имени
    @GetMapping("/search")
    public ResponseEntity<List<TagDto>> searchTags(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        List<TagDto> tags = tagService.searchTagDtos(q);
        return ResponseEntity.ok(tags);
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
    @PutMapping("/{id}")
    public ResponseEntity<Tag> updateTag(
            @PathVariable Long id,
            @RequestParam String name) {
        try {
            Tag updatedTag = tagService.updateTag(id, name);
            return ResponseEntity.ok(updatedTag);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Обновление тега через JSON
    @PutMapping("/{id}/update")
    public ResponseEntity<Tag> updateTagJson(
            @PathVariable Long id,
            @RequestBody Tag tagRequest) {
        try {
            Tag updatedTag = tagService.updateTag(id, tagRequest.getName());
            return ResponseEntity.ok(updatedTag);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Удаление тега
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        try {
            tagService.deleteTag(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
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