package com.example.artship.social.controller;

import com.example.artship.social.dto.ArtDto;
import com.example.artship.social.security.CustomUserDetails;
import com.example.artship.social.service.LikedArtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/liked-arts")
@Tag(name = "Liked Arts Controller", description = "API для управления понравившимися артами")
public class LikedArtController {
    
    private final LikedArtService likedArtService;
    
    public LikedArtController(LikedArtService likedArtService) {
        this.likedArtService = likedArtService;
    }
    
    //Проверить, добавлен ли арт в понравившиеся
    @GetMapping("/check/{artId}")
    @Operation(summary = "Проверить, добавлен ли арт в понравившиеся")
    public ResponseEntity<Map<String, Boolean>> isArtLiked(
            @PathVariable Long artId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        boolean isLiked = likedArtService.isArtLikedByUser(currentUser.getId(), artId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("liked", isLiked);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Получить все понравившиеся арты текущего пользователя
     */
    @GetMapping("/me")
    @Operation(summary = "Получить все понравившиеся арты текущего пользователя")
    public ResponseEntity<Page<ArtDto>> getMyLikedArts(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<ArtDto> arts = likedArtService.getLikedArtsByUser(currentUser.getId(), pageable);
        return ResponseEntity.ok(arts);
    }
    
    //Получить количество понравившихся артов пользователя
    @GetMapping("/count")
    @Operation(summary = "Получить количество понравившихся артов текущего пользователя")
    public ResponseEntity<Map<String, Long>> getLikedCount(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        Map<String, Long> response = new HashMap<>();
        response.put("count", likedArtService.getLikedArtsCount(currentUser.getId()));
        
        return ResponseEntity.ok(response);
    }
}