package com.example.artship.social.service;

import com.example.artship.social.dto.FollowDto;
import com.example.artship.social.model.Follow;
import com.example.artship.social.model.User;
import com.example.artship.social.repository.FollowRepository;
import com.example.artship.social.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FollowService {
    
    private static final Logger log = LoggerFactory.getLogger(FollowService.class);
    
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    
    public FollowService(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }
    
    // Подписка на пользователя
    public FollowDto followUser(Long followerId, Long followingId) {
        log.info("Following user: followerId={}, followingId={}", followerId, followingId);
        
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }
        
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Follower user not found with id: " + followerId));
        
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("Following user not found with id: " + followingId));
        
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new RuntimeException("User " + followerId + " is already following " + followingId);
        }
        
        Follow follow = new Follow(follower, following);
        Follow savedFollow = followRepository.save(follow);
        
        log.info("Follow created successfully: {} -> {}", 
                 follower.getUsername(), following.getUsername());
        
        return new FollowDto(savedFollow);
    }
    
    // Отписка от пользователя
    public void unfollowUser(Long followerId, Long followingId) {
        log.info("Unfollowing user: followerId={}, followingId={}", followerId, followingId);
        
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new RuntimeException(
                    "Follow relationship not found: " + followerId + " -> " + followingId));
        
        followRepository.delete(follow);
        log.info("Unfollowed successfully: {} -> {}", followerId, followingId);
    }
    
    // Проверка подписки
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }
    
    //Поиск среди подписчиков пользователя по username
    
    @Transactional(readOnly = true)
    public Page<FollowDto> searchFollowersByUsername(Long userId, String usernameSearch, Pageable pageable) {
        log.info("Searching followers for user {} by username: {}", userId, usernameSearch);
        
        if (usernameSearch == null || usernameSearch.trim().isEmpty()) {
            // Если поиск пустой, возвращаем всех подписчиков
            return getFollowers(userId, pageable);
        }
        
        Page<Follow> followersPage = followRepository.findByFollowingIdAndFollowerUsernameContainingIgnoreCase(
            userId, usernameSearch, pageable);
        
        List<FollowDto> dtos = followersPage.getContent().stream()
                .map(FollowDto::new)
                .collect(Collectors.toList());
        
        log.info("Found {} followers matching search", dtos.size());
        
        return new PageImpl<>(dtos, pageable, followersPage.getTotalElements());
    }
    

    @Transactional(readOnly = true)
    public Page<FollowDto> searchFollowingByUsername(Long userId, String usernameSearch, Pageable pageable) {
        log.info("Searching following for user {} by username: {}", userId, usernameSearch);
        
        if (usernameSearch == null || usernameSearch.trim().isEmpty()) {
            return getFollowing(userId, pageable);
        }
        
        // Поиск подписок, у которых username содержит поисковую строку
        Page<Follow> followingPage = followRepository.findByFollowerIdAndFollowingUsernameContainingIgnoreCase(
            userId, usernameSearch, pageable);
        
        List<FollowDto> dtos = followingPage.getContent().stream()
                .map(FollowDto::new)
                .collect(Collectors.toList());
        
        log.info("Found {} following matching search", dtos.size());
        
        return new PageImpl<>(dtos, pageable, followingPage.getTotalElements());
    }
    

    @Transactional(readOnly = true)
    public Page<FollowDto> searchFollowersByUsername(Long userId, String usernameSearch, 
                                                      Pageable pageable, boolean excludeCurrentUser) {
        log.info("Searching followers for user {} by username: {} (excludeCurrentUser={})", 
                 userId, usernameSearch, excludeCurrentUser);
        
        if (usernameSearch == null || usernameSearch.trim().isEmpty()) {
            Page<Follow> followersPage = getFollowersPage(userId, pageable);
            List<FollowDto> dtos = followersPage.getContent().stream()
                    .filter(follow -> !excludeCurrentUser || !follow.getFollower().getId().equals(userId))
                    .map(FollowDto::new)
                    .collect(Collectors.toList());
            return new PageImpl<>(dtos, pageable, followersPage.getTotalElements());
        }
        
        Page<Follow> followersPage = followRepository.findByFollowingIdAndFollowerUsernameContainingIgnoreCase(
            userId, usernameSearch, pageable);
        
        List<FollowDto> dtos = followersPage.getContent().stream()
                .filter(follow -> !excludeCurrentUser || !follow.getFollower().getId().equals(userId))
                .map(FollowDto::new)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, followersPage.getTotalElements());
    }
    
    
    // Получение подписчиков (с пагинацией)
    @Transactional(readOnly = true)
    public Page<FollowDto> getFollowers(Long userId, Pageable pageable) {
        log.debug("Getting followers for user {} with pagination: page={}, size={}", 
                 userId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Follow> followersPage = followRepository.findByFollowingId(userId, pageable);
        
        List<FollowDto> dtos = followersPage.getContent().stream()
                .map(FollowDto::new)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, followersPage.getTotalElements());
    }
    
    // Получение подписок (с пагинацией)
    @Transactional(readOnly = true)
    public Page<FollowDto> getFollowing(Long userId, Pageable pageable) {
        log.debug("Getting following for user {} with pagination: page={}, size={}", 
                 userId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Follow> followingPage = followRepository.findByFollowerId(userId, pageable);
        
        List<FollowDto> dtos = followingPage.getContent().stream()
                .map(FollowDto::new)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, followingPage.getTotalElements());
    }
    
    // Приватный метод для получения страницы подписчиков (для внутреннего использования)
    private Page<Follow> getFollowersPage(Long userId, Pageable pageable) {
        return followRepository.findByFollowingId(userId, pageable);
    }
    
    
    @Transactional(readOnly = true)
    public List<FollowDto> getFollowers(Long userId) {
        log.debug("Getting all followers for user {} (without pagination)", userId);
        
        return followRepository.findByFollowingId(userId).stream()
                .map(FollowDto::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<FollowDto> getFollowing(Long userId) {
        log.debug("Getting all following for user {} (without pagination)", userId);
        
        return followRepository.findByFollowerId(userId).stream()
                .map(FollowDto::new)
                .collect(Collectors.toList());
    }
    
    
    // Количество подписчиков
    @Transactional(readOnly = true)
    public Long getFollowerCount(Long userId) {
        return followRepository.countByFollowingId(userId);
    }
    
    // Количество подписок
    @Transactional(readOnly = true)
    public Long getFollowingCount(Long userId) {
        return followRepository.countByFollowerId(userId);
    }
    
 
     //Получить количество подписчиков, соответствующих поиску
    
    @Transactional(readOnly = true)
    public Long countFollowersBySearch(Long userId, String usernameSearch) {
        if (usernameSearch == null || usernameSearch.trim().isEmpty()) {
            return followRepository.countByFollowingId(userId);
        }
        return followRepository.countByFollowingIdAndFollowerUsernameContainingIgnoreCase(userId, usernameSearch);
    }
    
    
    // Получить количество подписок, соответствующих поиску
    @Transactional(readOnly = true)
    public Long countFollowingBySearch(Long userId, String usernameSearch) {
        if (usernameSearch == null || usernameSearch.trim().isEmpty()) {
            return followRepository.countByFollowerId(userId);
        }
        return followRepository.countByFollowerIdAndFollowingUsernameContainingIgnoreCase(userId, usernameSearch);
    }
}