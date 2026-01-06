package com.example.artship.social.controller;

import com.example.artship.social.dto.FollowDto;
import com.example.artship.social.service.FollowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follows")
public class FollowController {
    
    private static final Logger log = LoggerFactory.getLogger(FollowController.class);
    
    private final FollowService followService;
    
    public FollowController(FollowService followService) {
        this.followService = followService;
    }
    
    // Подписка на пользователя
    @PostMapping("/follower/{followerId}/following/{followingId}")
    public ResponseEntity<?> followUser(
            @PathVariable Long followerId,
            @PathVariable Long followingId) {
        log.info("Follow request: followerId={}, followingId={}", followerId, followingId);
        
        try {
            FollowDto follow = followService.followUser(followerId, followingId);
            log.info("User {} successfully followed user {}", followerId, followingId);
            
            // Возвращаем 201 Created с Location header
            URI location = URI.create("/api/follows/follower/" + followerId + "/following/" + followingId);
            return ResponseEntity.created(location).body(follow);
            
        } catch (RuntimeException e) {
            log.error("Error following user {} -> {}: {}", followerId, followingId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Отписка от пользователя
    @DeleteMapping("/follower/{followerId}/following/{followingId}")
    public ResponseEntity<?> unfollowUser(
            @PathVariable Long followerId,
            @PathVariable Long followingId) {
        log.info("Unfollow request: followerId={}, followingId={}", followerId, followingId);
        
        try {
            followService.unfollowUser(followerId, followingId);
            log.info("User {} successfully unfollowed user {}", followerId, followingId);
            return ResponseEntity.noContent().build();
            
        } catch (RuntimeException e) {
            log.error("Error unfollowing user {} -> {}: {}", followerId, followingId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    // Проверка подписки
    @GetMapping("/follower/{followerId}/following/{followingId}/exists")
    public ResponseEntity<Boolean> isFollowing(
            @PathVariable Long followerId,
            @PathVariable Long followingId) {
        log.debug("Checking if {} follows {}", followerId, followingId);
        
        boolean isFollowing = followService.isFollowing(followerId, followingId);
        return ResponseEntity.ok(isFollowing);
    }
    
    // Подписчики пользователя
    @GetMapping("/user/{userId}/followers")
    public ResponseEntity<List<FollowDto>> getFollowers(@PathVariable Long userId) {
        log.debug("Getting followers for user {}", userId);
        
        List<FollowDto> followers = followService.getFollowers(userId);
        return ResponseEntity.ok(followers);
    }
    
    // Подписки пользователя
    @GetMapping("/user/{userId}/following")
    public ResponseEntity<List<FollowDto>> getFollowing(@PathVariable Long userId) {
        log.debug("Getting following for user {}", userId);
        
        List<FollowDto> following = followService.getFollowing(userId);
        return ResponseEntity.ok(following);
    }
    
    // Количество подписчиков
    @GetMapping("/user/{userId}/followers/count")
    public ResponseEntity<Long> getFollowerCount(@PathVariable Long userId) {
        log.debug("Getting follower count for user {}", userId);
        
        Long count = followService.getFollowerCount(userId);
        return ResponseEntity.ok(count);
    }
    
    // Количество подписок
    @GetMapping("/user/{userId}/following/count")
    public ResponseEntity<Long> getFollowingCount(@PathVariable Long userId) {
        log.debug("Getting following count for user {}", userId);
        
        Long count = followService.getFollowingCount(userId);
        return ResponseEntity.ok(count);
    }
}