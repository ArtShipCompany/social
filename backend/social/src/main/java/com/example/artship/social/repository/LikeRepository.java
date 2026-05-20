package com.example.artship.social.repository;

import com.example.artship.social.model.ArtLikes;
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
public interface LikeRepository extends JpaRepository<ArtLikes, ArtLikes.LikeId> {
    
    // Методы с пагинацией
    @Query("SELECT l FROM ArtLikes l WHERE l.art.id = :artId")
    Page<ArtLikes> findByArtId(@Param("artId") Long artId, Pageable pageable);
    
    @Query("SELECT l FROM ArtLikes l WHERE l.user.id = :userId")
    Page<ArtLikes> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // Методы без пагинации (для обратной совместимости)
    @Query("SELECT l FROM ArtLikes l WHERE l.art.id = :artId")
    List<ArtLikes> findByArtId(@Param("artId") Long artId);
    
    @Query("SELECT l FROM ArtLikes l WHERE l.user.id = :userId")
    List<ArtLikes> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT l FROM ArtLikes l WHERE l.user.id = :userId AND l.art.id = :artId")
    Optional<ArtLikes> findByUserIdAndArtId(@Param("userId") Long userId, @Param("artId") Long artId);
    
    @Modifying
    @Query("DELETE FROM ArtLikes l WHERE l.user.id = :userId AND l.art.id = :artId")
    void deleteByUserIdAndArtId(@Param("userId") Long userId, @Param("artId") Long artId);
    
    @Query("SELECT COUNT(l) FROM ArtLikes l WHERE l.art.id = :artId")
    Long countByArtId(@Param("artId") Long artId);
    
    @Query("SELECT COUNT(l) FROM ArtLikes l WHERE l.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM ArtLikes l WHERE l.art.id = :artId")
    void deleteByArtId(@Param("artId") Long artId);
    
    boolean existsByUserIdAndArtId(Long userId, Long artId);

    void deleteByUserId(Long userId);
}