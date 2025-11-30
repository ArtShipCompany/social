package com.example.artship.social.controller;

import com.example.artship.social.dto.LikeDto;
import com.example.artship.social.service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/likes")
public class LikeController {
    
    private final LikeService likeService;
    
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }
    
    // Добавление лайка
    @PostMapping("/user/{userId}/art/{artId}")
    public ResponseEntity<LikeDto> addLike(
            @PathVariable Long userId,
            @PathVariable Long artId) {
        try {
            LikeDto like = likeService.addLike(userId, artId);
            return ResponseEntity.ok(like);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
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
    
    // Лайки арта
    @GetMapping("/art/{artId}")
    public ResponseEntity<List<LikeDto>> getLikesByArt(@PathVariable Long artId) {
        List<LikeDto> likes = likeService.getLikesByArtId(artId);
        return ResponseEntity.ok(likes);
    }
    
    // Лайки пользователя
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LikeDto>> getLikesByUser(@PathVariable Long userId) {
        List<LikeDto> likes = likeService.getLikesByUserId(userId);
        return ResponseEntity.ok(likes);
    }
    
    // Количество лайков арта
    @GetMapping("/art/{artId}/count")
    public ResponseEntity<Long> getLikeCountByArt(@PathVariable Long artId) {
        Long count = likeService.getLikeCountByArtId(artId);
        return ResponseEntity.ok(count);
    }
    
    // Количество лайков пользователя
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> getLikeCountByUser(@PathVariable Long userId) {
        Long count = likeService.getLikeCountByUserId(userId);
        return ResponseEntity.ok(count);
    }
}