package com.example.artship.social.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.artship.social.model.CommentLikes;

@Repository
public interface CommentLikesRepository extends JpaRepository<CommentLikes, CommentLikes.LikeId> {

    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    Optional <CommentLikes> findByUserIdAndCommentId(Long userId, Long commentId);

    Page<CommentLikes> findByCommentId(Long commentId, Pageable pageable);

    Page<CommentLikes> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT COUNT(cl) FROM CommentLikes cl WHERE cl.comment.id = :commentId")
    long countByCommentId(@Param("commentId") Long commentId);

    @Modifying
    void deleteByUserIdAndCommentId(Long userId, Long commentId);

    @Modifying
    void deleteByCommentId(Long commentId);

    @Modifying
    void deleteByUserId(Long userId);
    

}
