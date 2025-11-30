package com.example.artship.social.controller;

import com.example.artship.social.dto.FollowDto;
import com.example.artship.social.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follows")
public class FollowController {
    
    private final FollowService followService;
    
    public FollowController(FollowService followService) {
        this.followService = followService;
    }
    
    // Подписка на пользователя
    @PostMapping("/follower/{followerId}/following/{followingId}")
    public ResponseEntity<FollowDto> followUser(
            @PathVariable Long followerId,
            @PathVariable Long followingId) {
        try {
            FollowDto follow = followService.followUser(followerId, followingId);
            return ResponseEntity.ok(follow);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Отписка от пользователя
    @DeleteMapping("/follower/{followerId}/following/{followingId}")
    public ResponseEntity<Void> unfollowUser(
            @PathVariable Long followerId,
            @PathVariable Long followingId) {
        try {
            followService.unfollowUser(followerId, followingId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Проверка подписки
    @GetMapping("/follower/{followerId}/following/{followingId}/exists")
    public ResponseEntity<Boolean> isFollowing(
            @PathVariable Long followerId,
            @PathVariable Long followingId) {
        boolean isFollowing = followService.isFollowing(followerId, followingId);
        return ResponseEntity.ok(isFollowing);
    }
    
    // Подписчики пользователя
    @GetMapping("/user/{userId}/followers")
    public ResponseEntity<List<FollowDto>> getFollowers(@PathVariable Long userId) {
        List<FollowDto> followers = followService.getFollowers(userId);
        return ResponseEntity.ok(followers);
    }
    
    // Подписки пользователя
    @GetMapping("/user/{userId}/following")
    public ResponseEntity<List<FollowDto>> getFollowing(@PathVariable Long userId) {
        List<FollowDto> following = followService.getFollowing(userId);
        return ResponseEntity.ok(following);
    }
    
    // Количество подписчиков
    @GetMapping("/user/{userId}/followers/count")
    public ResponseEntity<Long> getFollowerCount(@PathVariable Long userId) {
        Long count = followService.getFollowerCount(userId);
        return ResponseEntity.ok(count);
    }
    
    // Количество подписок
    @GetMapping("/user/{userId}/following/count")
    public ResponseEntity<Long> getFollowingCount(@PathVariable Long userId) {
        Long count = followService.getFollowingCount(userId);
        return ResponseEntity.ok(count);
    }
    
    
}