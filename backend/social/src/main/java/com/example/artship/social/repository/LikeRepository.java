package com.example.artship.social.repository;

import com.example.artship.social.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Like.LikeId> {
    
    @Query("SELECT l FROM Like l WHERE l.art.id = :artId")
    List<Like> findByArtId(@Param("artId") Long artId);
    
    @Query("SELECT l FROM Like l WHERE l.user.id = :userId")
    List<Like> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT l FROM Like l WHERE l.user.id = :userId AND l.art.id = :artId")
    Optional<Like> findByUserIdAndArtId(@Param("userId") Long userId, @Param("artId") Long artId);
    
    @Modifying
    @Query("DELETE FROM Like l WHERE l.user.id = :userId AND l.art.id = :artId")
    void deleteByUserIdAndArtId(@Param("userId") Long userId, @Param("artId") Long artId);
    
    @Query("SELECT COUNT(l) FROM Like l WHERE l.art.id = :artId")
    Long countByArtId(@Param("artId") Long artId);
    
    @Query("SELECT COUNT(l) FROM Like l WHERE l.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    boolean existsByUserIdAndArtId(Long userId, Long artId);
}