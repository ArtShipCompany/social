package com.example.artship.social.controller;

import com.example.artship.social.dto.FollowDto;
import com.example.artship.social.security.CustomUserDetails;
import com.example.artship.social.service.FollowService;
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
@RequestMapping("/api/follow")
@Tag(name = "Follow", description = "API для управления подписками")
public class FollowController {
    
    private final FollowService followService;
    
    public FollowController(FollowService followService) {
        this.followService = followService;
    }
    
    
    @PostMapping("/{followingId}")
    @Operation(summary = "Подписаться на пользователя")
    public ResponseEntity<FollowDto> followUser(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long followingId) {
        
        FollowDto follow = followService.followUser(currentUser.getId(), followingId);
        return ResponseEntity.ok(follow);
    }
    
    @DeleteMapping("/{followingId}")
    @Operation(summary = "Отписаться от пользователя")
    public ResponseEntity<Map<String, String>> unfollowUser(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long followingId) {
        
        followService.unfollowUser(currentUser.getId(), followingId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Unfollowed successfully");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/check/{followingId}")
    @Operation(summary = "Проверить, подписан ли текущий пользователь")
    public ResponseEntity<Map<String, Boolean>> isFollowing(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long followingId) {
        
        boolean isFollowing = followService.isFollowing(currentUser.getId(), followingId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("isFollowing", isFollowing);
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping("/followers/{userId}/search")
    @Operation(summary = "Поиск среди подписчиков пользователя по username")
    public ResponseEntity<Page<FollowDto>> searchFollowers(
            @PathVariable Long userId,
            @RequestParam(required = false) String username,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<FollowDto> followers = followService.searchFollowersByUsername(userId, username, pageable);
        return ResponseEntity.ok(followers);
    }
    
    @GetMapping("/following/{userId}/search")
    @Operation(summary = "Поиск среди подписок пользователя по username")
    public ResponseEntity<Page<FollowDto>> searchFollowing(
            @PathVariable Long userId,
            @RequestParam(required = false) String username,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<FollowDto> following = followService.searchFollowingByUsername(userId, username, pageable);
        return ResponseEntity.ok(following);
    }
    
    // Поиск подписчиков текущего пользователя
    @GetMapping("/me/followers/search")
    @Operation(summary = "Поиск среди моих подписчиков по username")
    public ResponseEntity<Page<FollowDto>> searchMyFollowers(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false) String username,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<FollowDto> followers = followService.searchFollowersByUsername(currentUser.getId(), username, pageable);
        return ResponseEntity.ok(followers);
    }
    
    // Поиск подписок текущего пользователя
    @GetMapping("/me/following/search")
    @Operation(summary = "Поиск среди моих подписок по username")
    public ResponseEntity<Page<FollowDto>> searchMyFollowing(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false) String username,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<FollowDto> following = followService.searchFollowingByUsername(currentUser.getId(), username, pageable);
        return ResponseEntity.ok(following);
    }
    
    
    @GetMapping("/followers/{userId}")
    @Operation(summary = "Получить подписчиков пользователя")
    public ResponseEntity<Page<FollowDto>> getFollowers(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<FollowDto> followers = followService.getFollowers(userId, pageable);
        return ResponseEntity.ok(followers);
    }
    
    @GetMapping("/following/{userId}")
    @Operation(summary = "Получить подписки пользователя")
    public ResponseEntity<Page<FollowDto>> getFollowing(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<FollowDto> following = followService.getFollowing(userId, pageable);
        return ResponseEntity.ok(following);
    }
    
    @GetMapping("/me/followers")
    @Operation(summary = "Получить моих подписчиков")
    public ResponseEntity<Page<FollowDto>> getMyFollowers(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<FollowDto> followers = followService.getFollowers(currentUser.getId(), pageable);
        return ResponseEntity.ok(followers);
    }
    
    @GetMapping("/me/following")
    @Operation(summary = "Получить мои подписки")
    public ResponseEntity<Page<FollowDto>> getMyFollowing(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<FollowDto> following = followService.getFollowing(currentUser.getId(), pageable);
        return ResponseEntity.ok(following);
    }
    
    @GetMapping("/count/{userId}")
    @Operation(summary = "Получить количество подписчиков и подписок пользователя")
    public ResponseEntity<Map<String, Long>> getFollowCounts(@PathVariable Long userId) {
        Map<String, Long> counts = new HashMap<>();
        counts.put("followers", followService.getFollowerCount(userId));
        counts.put("following", followService.getFollowingCount(userId));
        return ResponseEntity.ok(counts);
    }
}