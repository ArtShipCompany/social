package com.example.artship.social.repository;

import com.example.artship.social.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByArtIdOrderByCreatedAtAsc(Long artId);
    
    List<Comment> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);
    
    List<Comment> findByArtIdAndParentCommentIsNullOrderByCreatedAtAsc(Long artId);
    
    @Query("SELECT c FROM Comment c WHERE c.art.id = :artId AND c.parentComment IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findRootCommentsByArtId(@Param("artId") Long artId);
    
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentCommentId ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentCommentId(@Param("parentCommentId") Long parentCommentId);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.art.id = :artId")
    Long countByArtId(@Param("artId") Long artId);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
}