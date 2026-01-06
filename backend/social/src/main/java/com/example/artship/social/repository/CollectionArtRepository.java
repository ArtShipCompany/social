package com.example.artship.social.repository;

import com.example.artship.social.model.CollectionArt;
import com.example.artship.social.model.CollectionArt.CollectionArtId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionArtRepository extends JpaRepository<CollectionArt, CollectionArtId> {
    
    // Найти связь по collectionId и artId
    @Query("SELECT ca FROM CollectionArt ca WHERE ca.collection.id = :collectionId AND ca.art.id = :artId")
    Optional<CollectionArt> findByCollectionIdAndArtId(@Param("collectionId") Long collectionId, 
                                                      @Param("artId") Long artId);
    
    // Проверить существование связи
    @Query("SELECT CASE WHEN COUNT(ca) > 0 THEN true ELSE false END " +
           "FROM CollectionArt ca WHERE ca.collection.id = :collectionId AND ca.art.id = :artId")
    boolean existsByCollectionIdAndArtId(@Param("collectionId") Long collectionId, 
                                        @Param("artId") Long artId);
    
    // Найти все арты в коллекции
    @Query("SELECT ca FROM CollectionArt ca WHERE ca.collection.id = :collectionId")
    List<CollectionArt> findByCollectionId(@Param("collectionId") Long collectionId);
    
    // Найти все коллекции, содержащие арт
    @Query("SELECT ca FROM CollectionArt ca WHERE ca.art.id = :artId")
    List<CollectionArt> findByArtId(@Param("artId") Long artId);
    
    // Количество артов в коллекции
    @Query("SELECT COUNT(ca) FROM CollectionArt ca WHERE ca.collection.id = :collectionId")
    Long countByCollectionId(@Param("collectionId") Long collectionId);
    
    // Количество коллекций, содержащих арт
    @Query("SELECT COUNT(ca) FROM CollectionArt ca WHERE ca.art.id = :artId")
    Long countByArtId(@Param("artId") Long artId);
    
    // Удалить связь по collectionId и artId
    @Modifying
    @Query("DELETE FROM CollectionArt ca WHERE ca.collection.id = :collectionId AND ca.art.id = :artId")
    void deleteByCollectionIdAndArtId(@Param("collectionId") Long collectionId, 
                                      @Param("artId") Long artId);
    
    // Удалить все связи по collectionId
    @Modifying
    @Query("DELETE FROM CollectionArt ca WHERE ca.collection.id = :collectionId")
    void deleteByCollectionId(@Param("collectionId") Long collectionId);
    
    // Удалить все связи по artId
    @Modifying
    @Query("DELETE FROM CollectionArt ca WHERE ca.art.id = :artId")
    void deleteByArtId(@Param("artId") Long artId);
}