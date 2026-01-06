package com.example.artship.social.repository;

import com.example.artship.social.model.Follow;
import com.example.artship.social.model.Follow.FollowId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, FollowId> {
    
    // Найти подписку по follower и following
    @Query("SELECT f FROM Follow f WHERE f.follower.id = :followerId AND f.following.id = :followingId")
    Optional<Follow> findByFollowerIdAndFollowingId(@Param("followerId") Long followerId, 
                                                    @Param("followingId") Long followingId);
    
    // Проверить существование подписки
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Follow f " +
           "WHERE f.follower.id = :followerId AND f.following.id = :followingId")
    boolean existsByFollowerIdAndFollowingId(@Param("followerId") Long followerId, 
                                             @Param("followingId") Long followingId);
    
    // Подписчики пользователя (кто подписан на него)
    @Query("SELECT f FROM Follow f WHERE f.following.id = :userId")
    List<Follow> findByFollowingId(@Param("userId") Long userId);
    
    // Подписки пользователя (на кого он подписан)
    @Query("SELECT f FROM Follow f WHERE f.follower.id = :userId")
    List<Follow> findByFollowerId(@Param("userId") Long userId);
    
    // Количество подписчиков
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.following.id = :userId")
    Long countByFollowingId(@Param("userId") Long userId);
    
    // Количество подписок
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :userId")
    Long countByFollowerId(@Param("userId") Long userId);
}