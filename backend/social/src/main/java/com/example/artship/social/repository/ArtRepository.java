package com.example.artship.social.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.artship.social.model.Art;
import com.example.artship.social.model.User;
import java.util.List;


@Repository
public interface ArtRepository extends JpaRepository<Art, Long> {

    Page<Art> findByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);
    List<Art> findByAuthorAndIsPublicTrueOrderByCreatedAtDesc(User author);
    Page<Art> findByAuthorAndIsPublicTrueOrderByCreatedAtDesc(User author, Pageable pageable);
    
    @Query("SELECT a FROM Art a WHERE a.author.id IN " +
           "(SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId) " +
           "AND a.isPublic = true ORDER BY a.createdAt DESC")
    Page<Art> findFeedByUserId(Long userId, Pageable pageable);

    @Query("SELECT a FROM Art a JOIN a.tags t WHERE t.name = :tagName AND a.isPublic = true ORDER BY a.createdAt DESC")
    Page<Art> findByTagNameAndIsPublicTrue(String tagName, Pageable pageable);

    List<Art> findByAuthorOrderByCreatedAtDesc(User author);

    Page<Art> findByTitleContainingIgnoreCaseAndIsPublicTrue(String title, Pageable pageable);
}