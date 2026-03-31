package com.example.artship.social.repository;

import com.example.artship.social.model.Art;
import com.example.artship.social.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ArtRepository extends JpaRepository<Art, Long> {
    
    Page<Art> findByAuthorAndIsPublicFlagTrue(User author, Pageable pageable);
    
    Page<Art> findByAuthor(User author, Pageable pageable);
    
    @Query("SELECT a FROM Art a WHERE a.isPublicFlag = true ORDER BY a.createdAt DESC")
    Page<Art> findByIsPublicFlagTrueOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT a FROM Art a WHERE a.isPublicFlag = true AND " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Art> findByTitleContainingIgnoreCaseAndIsPublicFlagTrue(@Param("title") String title, Pageable pageable);
    
    @Query("SELECT a FROM Art a JOIN a.tags t WHERE t.name = :tagName AND a.isPublicFlag = true")
    Page<Art> findByTagNameAndIsPublicFlagTrue(@Param("tagName") String tagName, Pageable pageable);
    
    @Query("SELECT a FROM Art a WHERE a.author.id IN " +
           "(SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId) " +
           "AND a.isPublicFlag = true ORDER BY a.createdAt DESC")
    Page<Art> findFeedByUserId(@Param("userId") Long userId, Pageable pageable);
    long countByTitleContainingIgnoreCaseAndIsPublicFlagTrue(String title);
    Page<Art> findByTagsId(Long tagId, Pageable pageable);
}