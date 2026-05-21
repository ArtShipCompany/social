package com.example.artship.social.controller;

import com.example.artship.social.dto.LikeDto;
import com.example.artship.social.service.LikeService;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/likes")
@Tag(name = "Like Controller", description = "API для управления лайками")
public class LikeController {
    
    private final LikeService likeService;
    
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }
    
    // Добавление лайка
    @PostMapping("/user/{userId}/art/{artId}")
    public ResponseEntity<?> addLike(
            @PathVariable Long userId,
            @PathVariable Long artId) {
        try {
            LikeDto like = likeService.addLike(userId, artId);
            return ResponseEntity.ok(like);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Удаление лайка
    @DeleteMapping("/user/{userId}/art/{artId}")
    public ResponseEntity<Void> removeLike(
            @PathVariable Long userId,
            @PathVariable Long artId) {
        try {
            likeService.removeLike(userId, artId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Проверка лайка
    @GetMapping("/user/{userId}/art/{artId}/exists")
    public ResponseEntity<Boolean> isLiked(
            @PathVariable Long userId,
            @PathVariable Long artId) {
        boolean isLiked = likeService.isLiked(userId, artId);
        return ResponseEntity.ok(isLiked);
    }
    
    // Лайки арта (с пагинацией)
    @GetMapping("/art/{artId}")
    public ResponseEntity<Page<LikeDto>> getLikesByArt(
            @PathVariable Long artId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<LikeDto> likes = likeService.getLikesByArtId(artId, pageable);
        return ResponseEntity.ok(likes);
    }
    
    
    // Количество лайков арта
    @GetMapping("/art/{artId}/count")
    public ResponseEntity<Long> getLikeCountByArt(@PathVariable Long artId) {
        Long count = likeService.getLikeCountByArtId(artId);
        return ResponseEntity.ok(count);
    }
    

}