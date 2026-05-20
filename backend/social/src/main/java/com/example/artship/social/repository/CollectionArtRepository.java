package com.example.artship.social.repository;

import com.example.artship.social.model.CollectionArt;
import com.example.artship.social.model.enumclass.ArtStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CollectionArtRepository extends JpaRepository<CollectionArt, Long> {
    
    // ==================== СУЩЕСТВУЮЩИЕ МЕТОДЫ ====================
    
    // Методы с пагинацией
    Page<CollectionArt> findByCollectionId(Long collectionId, Pageable pageable);
    Page<CollectionArt> findByArtId(Long artId, Pageable pageable);
    
    // Методы без пагинации (для обратной совместимости)
    List<CollectionArt> findByCollectionId(Long collectionId);
    List<CollectionArt> findByArtId(Long artId);
    
    Optional<CollectionArt> findByCollectionIdAndArtId(Long collectionId, Long artId);
    boolean existsByCollectionIdAndArtId(Long collectionId, Long artId);
    
    @Modifying
    void deleteByCollectionIdAndArtId(Long collectionId, Long artId);
    
    @Modifying
    void deleteByCollectionId(Long collectionId);
    
    Long countByCollectionId(Long collectionId);
    
    @Query("SELECT CASE WHEN COUNT(ca) > 0 THEN true ELSE false END FROM CollectionArt ca " +
           "WHERE ca.art.id = :artId AND ca.collection.user.id != :userId")
    boolean existsByArtIdAndUserIdNot(@Param("artId") Long artId, @Param("userId") Long userId);
    
    @Modifying
    void deleteByArtId(Long artId);
    
    
    /**
     * Получение артов коллекции с фильтром по статусу арта (с пагинацией)
     */
    @Query("SELECT ca FROM CollectionArt ca WHERE ca.collection.id = :collectionId AND ca.art.status = :status")
    Page<CollectionArt> findByCollectionIdAndArtStatus(@Param("collectionId") Long collectionId, 
                                                        @Param("status") ArtStatus status, 
                                                        Pageable pageable);
    
    /**
     * Получение артов коллекции с фильтром по списку статусов (с пагинацией)
     */
    @Query("SELECT ca FROM CollectionArt ca WHERE ca.collection.id = :collectionId AND ca.art.status IN :statuses")
    Page<CollectionArt> findByCollectionIdAndArtStatusIn(@Param("collectionId") Long collectionId, 
                                                          @Param("statuses") List<ArtStatus> statuses, 
                                                          Pageable pageable);
    
    /**
     * Получение артов коллекции без пагинации с фильтром по статусу
     */
    @Query("SELECT ca FROM CollectionArt ca WHERE ca.collection.id = :collectionId AND ca.art.status = :status")
    List<CollectionArt> findByCollectionIdAndArtStatus(@Param("collectionId") Long collectionId, 
                                                        @Param("status") ArtStatus status);
    
    /**
     * Подсчет количества артов в коллекции с фильтром по статусу
     */
    @Query("SELECT COUNT(ca) FROM CollectionArt ca WHERE ca.collection.id = :collectionId AND ca.art.status = :status")
    Long countByCollectionIdAndArtStatus(@Param("collectionId") Long collectionId, 
                                          @Param("status") ArtStatus status);
    
    /**
     * Подсчет количества артов в коллекции с фильтром по списку статусов
     */
    @Query("SELECT COUNT(ca) FROM CollectionArt ca WHERE ca.collection.id = :collectionId AND ca.art.status IN :statuses")
    Long countByCollectionIdAndArtStatusIn(@Param("collectionId") Long collectionId, 
                                            @Param("statuses") List<ArtStatus> statuses);
    
    /**
     * Проверка, есть ли у арта активные коллекции у других пользователей
     */
    @Query("SELECT CASE WHEN COUNT(ca) > 0 THEN true ELSE false END FROM CollectionArt ca " +
           "WHERE ca.art.id = :artId AND ca.collection.user.id != :userId AND ca.art.status = :status")
    boolean existsActiveByArtIdAndUserIdNot(@Param("artId") Long artId, 
                                             @Param("userId") Long userId,
                                             @Param("status") ArtStatus status);
    
    /**
     * Получение всех коллекций для арта с фильтром по статусу
     */
    @Query("SELECT ca FROM CollectionArt ca WHERE ca.art.id = :artId AND ca.art.status = :status")
    List<CollectionArt> findByArtIdAndArtStatus(@Param("artId") Long artId, 
                                                 @Param("status") ArtStatus status);
    
    /**
     * Получение всех коллекций для арта с фильтром по статусу (с пагинацией)
     */
    @Query("SELECT ca FROM CollectionArt ca WHERE ca.art.id = :artId AND ca.art.status = :status")
    Page<CollectionArt> findByArtIdAndArtStatus(@Param("artId") Long artId, 
                                                 @Param("status") ArtStatus status,
                                                 Pageable pageable);
    
    /**
     * Удаление всех связей для артов с определенным статусом
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM CollectionArt ca WHERE ca.art.status = :status")
    void deleteByArtStatus(@Param("status") ArtStatus status);
    
    /**
     * Удаление всех связей для артов пользователя
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM CollectionArt ca WHERE ca.art.author.id = :userId")
    void deleteByAuthorId(@Param("userId") Long userId);
}