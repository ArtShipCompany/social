package com.example.artship.social.controller;

import com.example.artship.social.dto.ArtDto;
import com.example.artship.social.dto.CollectionArtDto;
import com.example.artship.social.service.CollectionArtService;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/collection-arts")
public class CollectionArtController {

    private static final Logger log = LoggerFactory.getLogger(FollowController.class);

    
    private final CollectionArtService collectionArtService;
    
    public CollectionArtController(CollectionArtService collectionArtService) {
        this.collectionArtService = collectionArtService;
    }

    @PostMapping("/collection/{collectionId}/art/{artId}")
        public ResponseEntity<?> addArtToCollection(
                @PathVariable Long collectionId,
                @PathVariable Long artId) {
            log.info("Adding art {} to collection {}", artId, collectionId);
            
            try {
                CollectionArtDto collectionArt = collectionArtService.addArtToCollection(collectionId, artId);
                log.info("Successfully added art {} to collection {}", artId, collectionId);
                
                // Возвращаем 201 Created с Location header
                URI location = URI.create("/api/collection-arts/collection/" + collectionId + "/art/" + artId);
                return ResponseEntity.created(location).body(collectionArt);
                
            } catch (RuntimeException e) {
                log.error("Error adding art {} to collection {}: {}", artId, collectionId, e.getMessage(), e);
                Map<String, String> error = new HashMap<>();
                error.put("error", e.getMessage());
                return ResponseEntity.badRequest().body(error);
            }
        }
    
    // Удаление арта из коллекции
    @DeleteMapping("/collection/{collectionId}/art/{artId}")
    public ResponseEntity<Void> removeArtFromCollection(
            @PathVariable Long collectionId,
            @PathVariable Long artId) {
        try {
            collectionArtService.removeArtFromCollection(collectionId, artId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Получение артов коллекции
    @GetMapping("/collection/{collectionId}/arts")
    public ResponseEntity<List<ArtDto>> getArtsByCollection(@PathVariable Long collectionId) {
        List<ArtDto> arts = collectionArtService.getArtsByCollectionId(collectionId);
        return ResponseEntity.ok(arts);
    }
    
    // Получение коллекций арта
    @GetMapping("/art/{artId}/collections")
    public ResponseEntity<List<CollectionArtDto>> getCollectionsByArt(@PathVariable Long artId) {
        List<CollectionArtDto> collections = collectionArtService.getCollectionsByArtId(artId);
        return ResponseEntity.ok(collections);
    }
    
    // Получение CollectionArtDto для коллекции
    @GetMapping("/collection/{collectionId}/collection-arts")
    public ResponseEntity<List<CollectionArtDto>> getCollectionArts(@PathVariable Long collectionId) {
        List<CollectionArtDto> collectionArts = collectionArtService.getCollectionArtDtosByCollectionId(collectionId);
        return ResponseEntity.ok(collectionArts);
    }
    
    // Количество артов в коллекции
    @GetMapping("/collection/{collectionId}/arts/count")
    public ResponseEntity<Long> getArtCountByCollection(@PathVariable Long collectionId) {
        Long count = collectionArtService.getArtCountByCollectionId(collectionId);
        return ResponseEntity.ok(count);
    }
    
    // Проверка существования связи
    @GetMapping("/collection/{collectionId}/art/{artId}/exists")
    public ResponseEntity<Boolean> checkArtInCollection(
            @PathVariable Long collectionId,
            @PathVariable Long artId) {
        boolean exists = collectionArtService.existsByCollectionIdAndArtId(collectionId, artId);
        return ResponseEntity.ok(exists);
    }
    
    // Перемещение арта между коллекциями
    @PostMapping("/art/{artId}/move")
    public ResponseEntity<Void> moveArtBetweenCollections(
            @PathVariable Long artId,
            @RequestBody MoveArtRequest request) {
        try {
            collectionArtService.moveArtBetweenCollections(artId, request.getFromCollectionId(), request.getToCollectionId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Копирование арта в коллекцию
    @PostMapping("/art/{artId}/copy")
    public ResponseEntity<CollectionArtDto> copyArtToCollection(
            @PathVariable Long artId,
            @RequestBody CopyArtRequest request) {
        try {
            CollectionArtDto collectionArt = collectionArtService.copyArtToCollection(artId, request.getCollectionId());
            return ResponseEntity.ok(collectionArt);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // DTO для перемещения арта
    public static class MoveArtRequest {
        private Long fromCollectionId;
        private Long toCollectionId;
        
        public Long getFromCollectionId() { return fromCollectionId; }
        public void setFromCollectionId(Long fromCollectionId) { this.fromCollectionId = fromCollectionId; }
        
        public Long getToCollectionId() { return toCollectionId; }
        public void setToCollectionId(Long toCollectionId) { this.toCollectionId = toCollectionId; }
    }
    
    // DTO для копирования арта
    public static class CopyArtRequest {
        private Long collectionId;
        
        public Long getCollectionId() { return collectionId; }
        public void setCollectionId(Long collectionId) { this.collectionId = collectionId; }
    }
}