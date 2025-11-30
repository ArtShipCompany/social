package com.example.artship.social.repository;

import com.example.artship.social.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    Optional<Tag> findByName(String name);
    
    List<Tag> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT t FROM Tag t ORDER BY t.name ASC")
    List<Tag> findAllOrderByName();
    
    @Query("SELECT t FROM Tag t JOIN ArtTag at ON t.id = at.tag.id WHERE at.art.id = :artId")
    List<Tag> findByArtId(@Param("artId") Long artId);
    
    @Query("SELECT t FROM Tag t JOIN ArtTag at ON t.id = at.tag.id GROUP BY t.id ORDER BY COUNT(at.art.id) DESC")
    List<Tag> findPopularTags();
    
    @Query("SELECT COUNT(at) FROM ArtTag at WHERE at.tag.id = :tagId")
    Long countArtsByTagId(@Param("tagId") Long tagId);
    
    boolean existsByName(String name);
}