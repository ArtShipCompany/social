package com.example.artship.social.service;

import com.example.artship.social.dto.FollowDto;
import com.example.artship.social.model.Follow;
import com.example.artship.social.model.User;
import com.example.artship.social.repository.FollowRepository;
import com.example.artship.social.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FollowService {
    
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    
    public FollowService(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }
    
    // Подписка на пользователя
    public FollowDto followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new RuntimeException("Cannot follow yourself");
        }
        
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Follower not found with id: " + followerId));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("Following not found with id: " + followingId));
        
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new RuntimeException("Already following");
        }
        
        Follow follow = new Follow(follower, following);
        Follow savedFollow = followRepository.save(follow);
        return new FollowDto(savedFollow);
    }
    
    // Отписка от пользователя
    public void unfollowUser(Long followerId, Long followingId) {
        followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
    }
    
    // Проверка подписки
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }
    
    // Получение подписчиков
    @Transactional(readOnly = true)
    public List<FollowDto> getFollowers(Long userId) {
        return followRepository.findByFollowingId(userId).stream()
                .map(FollowDto::new)
                .collect(Collectors.toList());
    }
    
    // Получение подписок
    @Transactional(readOnly = true)
    public List<FollowDto> getFollowing(Long userId) {
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
}