package com.example.artship.social.repository;

import com.example.artship.social.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Методы с пагинацией
    Page<Comment> findByArtIdOrderByCreatedAtAsc(Long artId, Pageable pageable);
    
    Page<Comment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    Page<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId, Pageable pageable);
    
    @Query("SELECT c FROM Comment c WHERE c.art.id = :artId AND c.parentComment IS NULL ORDER BY c.createdAt ASC")
    Page<Comment> findRootCommentsByArtId(@Param("artId") Long artId, Pageable pageable);
    
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentCommentId ORDER BY c.createdAt ASC")
    Page<Comment> findRepliesByParentCommentId(@Param("parentCommentId") Long parentCommentId, Pageable pageable);
    
    // Методы для подсчета
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.art.id = :artId")
    Long countByArtId(@Param("artId") Long artId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parentComment.id = :parentCommentId")
    Long countByParentCommentId(@Param("parentCommentId") Long parentCommentId);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
}