package com.example.artship.social.repository;

import com.example.artship.social.model.CommentLikes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikesRepository extends JpaRepository<CommentLikes, CommentLikes.CommentLikeId> {

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);
    
    @Query("SELECT cl FROM CommentLikes cl WHERE cl.user.id = :userId AND cl.comment.id = :commentId")
    Page<CommentLikes> findByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId, Pageable pageable);
    
    Page<CommentLikes> findByCommentId(Long commentId, Pageable pageable);
    
    Page<CommentLikes> findByUserId(Long userId, Pageable pageable);

    @Modifying
    void deleteByUserIdAndCommentId(Long userId, Long commentId);

    @Modifying
    void deleteByCommentId(Long commentId);

    @Modifying
    void deleteByUserId(Long userId);
    
    @Query("SELECT COUNT(cl) FROM CommentLikes cl WHERE cl.comment.id = :commentId")
    long countByCommentId(@Param("commentId") Long commentId);
}