package com.example.artship.social.controller;

import com.example.artship.social.model.SocialPlatform;
import com.example.artship.social.requests.SocialLinkRequest;
import com.example.artship.social.response.SocialLinkResponse;
import com.example.artship.social.security.CustomUserDetails;
import com.example.artship.social.service.SocialLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/social-links")
@Tag(name = "Social Links", description = "API для управления ссылками на социальные сети")
public class SocialLinkController {
    
    @Autowired
    private SocialLinkService socialLinkService;

    
    
    // Получить все ссылки текущего пользователя
    @GetMapping("/me")
    @Operation(summary = "Получить мои социальные ссылки")
    public ResponseEntity<?> getMySocialLinks(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = "false") boolean onlyVisible) {
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not authenticated"));
        }
        
        List<SocialLinkResponse> links = socialLinkService.getUserSocialLinks(
            currentUser.getId(), 
            onlyVisible
        );
        
        return ResponseEntity.ok(Map.of(
            "socialLinks", links,
            "count", links.size()
        ));
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Получить социальные ссылки пользователя")
    public ResponseEntity<?> getUserSocialLinks(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "true") boolean onlyVisible) {
        
        List<SocialLinkResponse> links = socialLinkService.getUserSocialLinks(userId, onlyVisible);
        
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "socialLinks", links,
            "count", links.size()
        ));
    }
    
    @PostMapping
    @Operation(summary = "Добавить социальную ссылку")
    public ResponseEntity<?> addSocialLink(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody SocialLinkRequest request) {  
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not authenticated"));
        }
        
        try {
            SocialLinkResponse created = socialLinkService.addSocialLink(
                currentUser.getId(), 
                request 
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/{linkId}")
    @Operation(summary = "Обновить социальную ссылку")
    public ResponseEntity<?> updateSocialLink(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long linkId,
            @Valid @RequestBody SocialLinkRequest request) { 
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not authenticated"));
        }
        
        try {
            SocialLinkResponse updated = socialLinkService.updateSocialLink(
                currentUser.getId(), 
                linkId, 
                request  
            );
            
            return ResponseEntity.ok(updated);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Удалить ссылку
    @DeleteMapping("/{linkId}")
    @Operation(summary = "Удалить социальную ссылку")
    public ResponseEntity<?> deleteSocialLink(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long linkId) {
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not authenticated"));
        }
        
        try {
            socialLinkService.deleteSocialLink(currentUser.getId(), linkId);
            return ResponseEntity.ok(Map.of("message", "Social link deleted successfully"));
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Обновить порядок отображения
    @PutMapping("/order")
    @Operation(summary = "Обновить порядок отображения ссылок")
    public ResponseEntity<?> updateDisplayOrder(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody List<Long> linkIds) {
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not authenticated"));
        }
        
        try {
            socialLinkService.updateDisplayOrder(currentUser.getId(), linkIds);
            return ResponseEntity.ok(Map.of("message", "Display order updated successfully"));
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Массовое обновление всех ссылок - используем List<SocialLinkRequest>
    @PutMapping("/batch")
    @Operation(summary = "Массовое обновление всех ссылок")
    public ResponseEntity<?> updateAllSocialLinks(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody List<SocialLinkRequest> requests) {  
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not authenticated"));
        }
        
        try {
            List<SocialLinkResponse> updated = socialLinkService.updateAllSocialLinks(
                currentUser.getId(), 
                requests  
            );
            
            return ResponseEntity.ok(Map.of(
                "socialLinks", updated,
                "message", "All social links updated successfully"
            ));
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Получить доступные платформы
    @GetMapping("/platforms")
    @Operation(summary = "Получить список доступных платформ")
    public ResponseEntity<?> getAvailablePlatforms() {
        
        List<Map<String, String>> platforms = java.util.Arrays.stream(SocialPlatform.values())
            .map(platform -> {
                Map<String, String> platformInfo = new HashMap<>();
                platformInfo.put("name", platform.name());
                platformInfo.put("displayName", platform.getDisplayName());
                platformInfo.put("baseUrl", platform.getBaseUrl());
                return platformInfo;
            })
            .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(Map.of("platforms", platforms));
    }
    
    // Валидация URL
    @PostMapping("/validate")
    @Operation(summary = "Проверить валидность URL")
    public ResponseEntity<?> validateUrl(
            @RequestParam String url,
            @RequestParam SocialPlatform platform) {
        
        boolean isValid = socialLinkService.validateUrl(url, platform);
        
        return ResponseEntity.ok(Map.of(
            "url", url,
            "platform", platform,
            "valid", isValid
        ));
    }
}