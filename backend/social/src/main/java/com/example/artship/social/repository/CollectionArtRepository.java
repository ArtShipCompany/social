package com.example.artship.social.repository;

import com.example.artship.social.model.CollectionArt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionArtRepository extends JpaRepository<CollectionArt, CollectionArt.CollectionArtId> {
    
    @Query("SELECT ca FROM CollectionArt ca WHERE ca.collection.id = :collectionId ORDER BY ca.savedAt DESC")
    List<CollectionArt> findByCollectionId(@Param("collectionId") Long collectionId);
    
    @Query("SELECT ca FROM CollectionArt ca WHERE ca.art.id = :artId ORDER BY ca.savedAt DESC")
    List<CollectionArt> findByArtId(@Param("artId") Long artId);
    
    @Query("SELECT ca FROM CollectionArt ca WHERE ca.collection.id = :collectionId AND ca.art.id = :artId")
    List<CollectionArt> findByCollectionIdAndArtId(@Param("collectionId") Long collectionId, @Param("artId") Long artId);
    
    @Modifying
    @Query("DELETE FROM CollectionArt ca WHERE ca.collection.id = :collectionId AND ca.art.id = :artId")
    void deleteByCollectionIdAndArtId(@Param("collectionId") Long collectionId, @Param("artId") Long artId);
    
    @Modifying
    @Query("DELETE FROM CollectionArt ca WHERE ca.collection.id = :collectionId")
    void deleteByCollectionId(@Param("collectionId") Long collectionId);
    
    @Query("SELECT COUNT(ca) FROM CollectionArt ca WHERE ca.collection.id = :collectionId")
    Long countByCollectionId(@Param("collectionId") Long collectionId);
    
    boolean existsByCollectionIdAndArtId(Long collectionId, Long artId);
}