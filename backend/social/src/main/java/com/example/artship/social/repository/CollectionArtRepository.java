package com.example.artship.social.repository;

import com.example.artship.social.model.CollectionArt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CollectionArtRepository extends JpaRepository<CollectionArt, Long> {
    // Методы с пагинацией
    Page<CollectionArt> findByCollectionId(Long collectionId, Pageable pageable);
    Page<CollectionArt> findByArtId(Long artId, Pageable pageable);
    
    // Методы без пагинации (для обратной совместимости)
    List<CollectionArt> findByCollectionId(Long collectionId);
    List<CollectionArt> findByArtId(Long artId);
    
    Optional<CollectionArt> findByCollectionIdAndArtId(Long collectionId, Long artId);
    boolean existsByCollectionIdAndArtId(Long collectionId, Long artId);
    void deleteByCollectionIdAndArtId(Long collectionId, Long artId);
    void deleteByCollectionId(Long collectionId);
    Long countByCollectionId(Long collectionId);
}