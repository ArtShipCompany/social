package com.example.artship.social.repository;

import com.example.artship.social.model.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Like.LikeId> {
    
    // Методы с пагинацией
    @Query("SELECT l FROM Like l WHERE l.art.id = :artId")
    Page<Like> findByArtId(@Param("artId") Long artId, Pageable pageable);
    
    @Query("SELECT l FROM Like l WHERE l.user.id = :userId")
    Page<Like> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // Методы без пагинации (для обратной совместимости)
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

    @Modifying
    @Query("DELETE FROM Like l WHERE l.art.id = :artId")
    void deleteByArtId(@Param("artId") Long artId);
    
    boolean existsByUserIdAndArtId(Long userId, Long artId);
}