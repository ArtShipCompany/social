package com.example.artship.social.controller;

import com.example.artship.social.dto.ArtDto;
import com.example.artship.social.dto.ArtTagDto;
import com.example.artship.social.dto.TagDto;
import com.example.artship.social.service.ArtTagService;
import com.example.artship.social.service.TagManagementService;
import com.example.artship.social.service.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/art-tags")
public class ArtTagController {
    
    private static final Logger log = LoggerFactory.getLogger(ArtTagController.class);
    
    private final ArtTagService artTagService;
    private final TagManagementService tagManagementService; 
    private final TagService tagService;
    
    public ArtTagController(ArtTagService artTagService, 
                           TagManagementService tagManagementService,
                           TagService tagService) {
        this.artTagService = artTagService;
        this.tagManagementService = tagManagementService;
        this.tagService = tagService;
    }
    
    
    // Добавление тега к арту (возвращает ArtTagDto)
    @PostMapping("/art/{artId}/tag/{tagId}")
    public ResponseEntity<?> addTagToArt(
            @PathVariable Long artId,
            @PathVariable Long tagId) {
        log.info("Adding tag {} to art {}", tagId, artId);
        
        try {
            ArtTagDto artTagDto = artTagService.createArtTag(artId, tagId);
            log.info("Successfully added tag {} to art {}", tagId, artId);
            
            URI location = URI.create("/api/art-tags/art/" + artId + "/tag/" + tagId);
            return ResponseEntity.created(location).body(artTagDto);
            
        } catch (RuntimeException e) {
            log.error("Error adding tag {} to art {}: {}", tagId, artId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Удаление тега из арта
    @DeleteMapping("/art/{artId}/tag/{tagId}")
    public ResponseEntity<?> removeTagFromArt(
            @PathVariable Long artId,
            @PathVariable Long tagId) {
        log.info("Removing tag {} from art {}", tagId, artId);
        
        try {
            artTagService.removeTagFromArt(artId, tagId);
            log.info("Successfully removed tag {} from art {}", tagId, artId);
            return ResponseEntity.noContent().build();
            
        } catch (RuntimeException e) {
            log.error("Error removing tag {} from art {}: {}", tagId, artId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    // Проверка связи арт-тег
    @GetMapping("/art/{artId}/tag/{tagId}/exists")
    public ResponseEntity<Boolean> checkTagArtRelation(
            @PathVariable Long artId,
            @PathVariable Long tagId) {
        log.debug("Checking if tag {} exists in art {}", tagId, artId);
        
        boolean exists = artTagService.existsByArtIdAndTagId(artId, tagId);
        return ResponseEntity.ok(exists);
    }
    

    
    // Получение тегов арта
    @GetMapping("/art/{artId}/tags")
    public ResponseEntity<List<TagDto>> getTagsByArt(@PathVariable Long artId) {
        log.debug("Getting tags for art {}", artId);
        
        List<TagDto> tags = tagManagementService.getTagsByArtId(artId);
        return ResponseEntity.ok(tags);
    }
    
    // Получение артов по тегу
    @GetMapping("/tag/{tagId}/arts")
    public ResponseEntity<List<ArtDto>> getArtsByTag(@PathVariable Long tagId) {
        log.debug("Getting arts for tag {}", tagId);

        List<ArtDto> arts = tagManagementService.getArtsByTagId(tagId);
        return ResponseEntity.ok(arts);
    }
    
    //Получение всех ArtTagDto для арта
    @GetMapping("/art/{artId}/art-tags")
    public ResponseEntity<List<ArtTagDto>> getArtTagsByArt(@PathVariable Long artId) {
        log.debug("Getting art-tag relations for art {}", artId);
        
        List<ArtTagDto> artTags = artTagService.getArtTagDtosByArtId(artId);
        return ResponseEntity.ok(artTags);
    }
    
    // Получение всех ArtTagDto для тега
    @GetMapping("/tag/{tagId}/art-tags")
    public ResponseEntity<List<ArtTagDto>> getArtTagsByTag(@PathVariable Long tagId) {
        log.debug("Getting art-tag relations for tag {}", tagId);
        
        List<ArtTagDto> artTags = artTagService.getArtTagDtosByTagId(tagId);
        return ResponseEntity.ok(artTags);
    }
    

    
    // Количество артов по тегу
    @GetMapping("/tag/{tagId}/arts/count")
    public ResponseEntity<Long> getArtCountByTag(@PathVariable Long tagId) {
        log.debug("Getting art count for tag {}", tagId);
        
        Long count = artTagService.getArtCountByTagId(tagId);
        return ResponseEntity.ok(count);
    }
    
    // Количество тегов у арта
    @GetMapping("/art/{artId}/tags/count")
    public ResponseEntity<Long> getTagCountByArt(@PathVariable Long artId) {
        log.debug("Getting tag count for art {}", artId);
        
        Long count = artTagService.getTagCountByArtId(artId);
        return ResponseEntity.ok(count);
    }
    
    //Удаление всех тегов из арта
    @DeleteMapping("/art/{artId}/tags")
    public ResponseEntity<?> removeAllTagsFromArt(@PathVariable Long artId) {
        log.info("Removing all tags from art {}", artId);
        
        try {
            artTagService.removeAllTagsFromArt(artId);
            log.info("Successfully removed all tags from art {}", artId);
            return ResponseEntity.noContent().build();
            
        } catch (RuntimeException e) {
            log.error("Error removing all tags from art {}: {}", artId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    
    // Массовое добавление тегов к арту
    @PostMapping("/art/{artId}/tags/batch")
    public ResponseEntity<?> addTagsBatchToArt(
            @PathVariable Long artId,
            @RequestBody AddTagsRequest request) {
        log.info("Adding {} tags to art {}", request.getTagNames().size(), artId);
        
        try {

            tagManagementService.addTagsToArt(artId, request.getTagNames());
            
            log.info("Successfully added {} tags to art {}", request.getTagNames().size(), artId);

            return ResponseEntity.ok().build();
            
        } catch (RuntimeException e) {
            log.error("Error adding tags to art {}: {}", artId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Получение ArtTagDto по artId и tagId
    @GetMapping("/art/{artId}/tag/{tagId}")
    public ResponseEntity<?> getArtTagRelation(
            @PathVariable Long artId,
            @PathVariable Long tagId) {
        log.debug("Getting art-tag relation: artId={}, tagId={}", artId, tagId);
        
        try {
            boolean exists = artTagService.existsByArtIdAndTagId(artId, tagId);
            if (!exists) {
                return ResponseEntity.notFound().build();
            }
            List<ArtTagDto> relations = artTagService.getArtTagDtosByArtId(artId).stream()
                    .filter(artTag -> artTag.getTagId().equals(tagId))
                    .collect(java.util.stream.Collectors.toList());
            
            if (relations.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(relations.get(0));
            
        } catch (RuntimeException e) {
            log.error("Error getting art-tag relation: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    public static class AddTagsRequest {
        private List<String> tagNames;
        
        public List<String> getTagNames() {
            return tagNames;
        }
        
        public void setTagNames(List<String> tagNames) {
            this.tagNames = tagNames;
        }
        
        @Override
        public String toString() {
            return "AddTagsRequest{" +
                   "tagNames=" + tagNames +
                   '}';
        }
    }
}