package com.example.artship.social.repository;

import com.example.artship.social.model.Collection;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    
    // Методы с пагинацией
    Page<Collection> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    Page<Collection> findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    Page<Collection> findByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT c FROM Collection c WHERE c.isPublic = true AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY c.createdAt DESC")
    Page<Collection> searchPublicCollections(@Param("query") String query, Pageable pageable);
    
    // Методы без пагинации (для обратной совместимости, если нужны)
    List<Collection> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Collection> findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(Long userId);
    
    List<Collection> findByIsPublicTrueOrderByCreatedAtDesc();
    
    @Query("SELECT c FROM Collection c WHERE c.isPublic = true AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY c.createdAt DESC")
    List<Collection> searchPublicCollections(@Param("query") String query);
    
    @Query("SELECT COUNT(ca) FROM CollectionArt ca WHERE ca.collection.id = :collectionId")
    Long countArtsByCollectionId(@Param("collectionId") Long collectionId);
    
    boolean existsByUserIdAndTitle(Long userId, String title);
}