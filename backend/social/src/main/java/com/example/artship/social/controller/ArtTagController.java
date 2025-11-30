package com.example.artship.social.controller;

import com.example.artship.social.dto.ArtDto;
import com.example.artship.social.dto.ArtTagDto;
import com.example.artship.social.dto.TagDto;
import com.example.artship.social.model.Art;
import com.example.artship.social.service.ArtService;
import com.example.artship.social.service.ArtTagService;
import com.example.artship.social.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/art-tags")
public class ArtTagController {
    
    private final ArtTagService artTagService;
    private final ArtService artService;
    private final TagService tagService;
    
    public ArtTagController(ArtTagService artTagService, ArtService artService, TagService tagService) {
        this.artTagService = artTagService;
        this.artService = artService;
        this.tagService = tagService;
    }
    
    // Добавление тега к арту (возвращает ArtTagDto)
    @PostMapping("/art/{artId}/tag/{tagId}")
    public ResponseEntity<ArtTagDto> addTagToArt(
            @PathVariable Long artId,
            @PathVariable Long tagId) {
        try {
            ArtTagDto artTagDto = artTagService.createArtTagWithDto(artId, tagId);
            return ResponseEntity.ok(artTagDto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Добавление тегов к арту по именам
    @PostMapping("/art/{artId}/tags")
    public ResponseEntity<ArtDto> addTagsToArt(
            @PathVariable Long artId,
            @RequestBody List<String> tagNames) {
        try {
            ArtDto art = artService.addTagsToArt(artId, tagNames);
            return ResponseEntity.ok(art);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Удаление тега из арта
    @DeleteMapping("/art/{artId}/tag/{tagId}")
    public ResponseEntity<ArtDto> removeTagFromArt(
            @PathVariable Long artId,
            @PathVariable Long tagId) {
        try {
            ArtDto art = artService.removeTagFromArt(artId, tagId);
            return ResponseEntity.ok(art);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Удаление всех тегов из арта
    @DeleteMapping("/art/{artId}/tags")
    public ResponseEntity<Void> removeAllTagsFromArt(@PathVariable Long artId) {
        try {
            artTagService.removeAllTagsFromArt(artId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Получение тегов арта
    @GetMapping("/art/{artId}/tags")
    public ResponseEntity<List<TagDto>> getArtTags(@PathVariable Long artId) {
        try {
            List<TagDto> tags = artService.getArtTags(artId);
            return ResponseEntity.ok(tags);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Получение ArtTagDto для арта
    @GetMapping("/art/{artId}/art-tag-dtos")
    public ResponseEntity<List<ArtTagDto>> getArtTagDtosByArt(@PathVariable Long artId) {
        try {
            List<ArtTagDto> artTagDtos = artTagService.getArtTagDtosByArtId(artId);
            return ResponseEntity.ok(artTagDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Получение ArtTagDto для тега
    @GetMapping("/tag/{tagId}/art-tag-dtos")
    public ResponseEntity<List<ArtTagDto>> getArtTagDtosByTag(@PathVariable Long tagId) {
        try {
            List<ArtTagDto> artTagDtos = artTagService.getArtTagDtosByTagId(tagId);
            return ResponseEntity.ok(artTagDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Получение всех ArtTagDto
    @GetMapping
    public ResponseEntity<List<ArtTagDto>> getAllArtTagDtos() {
        List<ArtTagDto> artTagDtos = artTagService.getAllArtTagDtos();
        return ResponseEntity.ok(artTagDtos);
    }
    
    // Получение артов по тегу
    @GetMapping("/tag/{tagId}/arts")
    public ResponseEntity<List<Art>> getArtsByTag(@PathVariable Long tagId) {
        try {
            List<Art> arts = artTagService.getArtsByTagId(tagId);
            return ResponseEntity.ok(arts);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Получение DTO артов по тегу
    @GetMapping("/tag/{tagId}/arts-dto")
    public ResponseEntity<List<ArtDto>> getArtsDtoByTag(@PathVariable Long tagId) {
        try {
            List<Art> arts = artTagService.getArtsByTagId(tagId);
            List<ArtDto> artDtos = arts.stream()
                    .map(art -> artService.getArtDtoById(art.getId()).orElse(null))
                    .filter(artDto -> artDto != null)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(artDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Проверка существования связи
    @GetMapping("/art/{artId}/tag/{tagId}/exists")
    public ResponseEntity<Boolean> tagArtRelationExists(
            @PathVariable Long artId,
            @PathVariable Long tagId) {
        boolean exists = artTagService.existsByArtIdAndTagId(artId, tagId);
        return ResponseEntity.ok(exists);
    }
    
    // Массовое добавление тегов к арту
    @PostMapping("/art/{artId}/tags/batch")
    public ResponseEntity<ArtDto> addTagsBatchToArt(
            @PathVariable Long artId,
            @RequestBody TagBatchRequest request) {
        try {
            ArtDto art = artService.addTagsToArt(artId, request.getTagNames());
            return ResponseEntity.ok(art);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Получение количества артов по тегу
    @GetMapping("/tag/{tagId}/arts/count")
    public ResponseEntity<Long> getArtCountByTag(@PathVariable Long tagId) {
        try {
            List<Art> arts = artTagService.getArtsByTagId(tagId);
            return ResponseEntity.ok((long) arts.size());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Получение популярных связей (тегов с наибольшим количеством артов)
    @GetMapping("/popular-relations")
    public ResponseEntity<List<TagDto>> getPopularTagRelations(
            @RequestParam(defaultValue = "10") int limit) {
        List<TagDto> popularTags = tagService.getPopularTagDtos();
        if (popularTags.size() > limit) {
            popularTags = popularTags.subList(0, limit);
        }
        return ResponseEntity.ok(popularTags);
    }
    
    // Получение ArtTagDto по artId и tagId
    @GetMapping("/art/{artId}/tag/{tagId}")
    public ResponseEntity<List<ArtTagDto>> getArtTagDtosByArtAndTag(
            @PathVariable Long artId,
            @PathVariable Long tagId) {
        try {
            List<ArtTagDto> artTagDtos = artTagService.getArtTagDtosByArtIdAndTagId(artId, tagId);
            return ResponseEntity.ok(artTagDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // DTO для массового добавления тегов
    public static class TagBatchRequest {
        private List<String> tagNames;
        
        public List<String> getTagNames() {
            return tagNames;
        }
        
        public void setTagNames(List<String> tagNames) {
            this.tagNames = tagNames;
        }
    }
}