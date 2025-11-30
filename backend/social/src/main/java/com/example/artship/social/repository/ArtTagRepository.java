package com.example.artship.social.repository;

import com.example.artship.social.model.ArtTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtTagRepository extends JpaRepository<ArtTag, ArtTag.ArtTagId> {
    
    @Query("SELECT at FROM ArtTag at WHERE at.art.id = :artId")
    List<ArtTag> findByArtId(@Param("artId") Long artId);
    
    @Query("SELECT at FROM ArtTag at WHERE at.tag.id = :tagId")
    List<ArtTag> findByTagId(@Param("tagId") Long tagId);
    
    @Query("SELECT at FROM ArtTag at WHERE at.art.id = :artId AND at.tag.id = :tagId")
    List<ArtTag> findByArtIdAndTagId(@Param("artId") Long artId, @Param("tagId") Long tagId);
    
    @Modifying
    @Query("DELETE FROM ArtTag at WHERE at.art.id = :artId AND at.tag.id = :tagId")
    void deleteByArtIdAndTagId(@Param("artId") Long artId, @Param("tagId") Long tagId);
    
    @Modifying
    @Query("DELETE FROM ArtTag at WHERE at.art.id = :artId")
    void deleteByArtId(@Param("artId") Long artId);
    
    @Modifying
    @Query("DELETE FROM ArtTag at WHERE at.tag.id = :tagId")
    void deleteByTagId(@Param("tagId") Long tagId);
    
    boolean existsByArtIdAndTagId(Long artId, Long tagId);
}