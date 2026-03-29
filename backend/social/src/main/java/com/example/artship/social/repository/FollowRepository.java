package com.example.artship.social.repository;

import com.example.artship.social.model.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    
    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);
    
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);
    
    // Получение всех подписчиков пользователя (кто подписан на userId)
    List<Follow> findByFollowingId(Long followingId);
    Page<Follow> findByFollowingId(Long followingId, Pageable pageable);
    
    // Получение всех подписок пользователя (на кого подписан userId)
    List<Follow> findByFollowerId(Long followerId);
    Page<Follow> findByFollowerId(Long followerId, Pageable pageable);
    
    // Подсчет количества
    Long countByFollowingId(Long followingId);
    Long countByFollowerId(Long followerId);
        
    // Поиск среди подписчиков по username
    @Query("SELECT f FROM Follow f WHERE f.following.id = :userId AND LOWER(f.follower.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    Page<Follow> findByFollowingIdAndFollowerUsernameContainingIgnoreCase(
            @Param("userId") Long userId, 
            @Param("username") String username, 
            Pageable pageable);
    
    // Поиск среди подписок по username
    @Query("SELECT f FROM Follow f WHERE f.follower.id = :userId AND LOWER(f.following.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    Page<Follow> findByFollowerIdAndFollowingUsernameContainingIgnoreCase(
            @Param("userId") Long userId, 
            @Param("username") String username, 
            Pageable pageable);
    
    // Подсчет подписчиков, соответствующих поиску
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.following.id = :userId AND LOWER(f.follower.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    Long countByFollowingIdAndFollowerUsernameContainingIgnoreCase(
            @Param("userId") Long userId, 
            @Param("username") String username);
    
    // Подсчет подписок, соответствующих поиску
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :userId AND LOWER(f.following.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    Long countByFollowerIdAndFollowingUsernameContainingIgnoreCase(
            @Param("userId") Long userId, 
            @Param("username") String username);
}