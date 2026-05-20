package com.example.artship.social.repository;

import com.example.artship.social.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
        
    Page<Comment> findByArtIdOrderByCreatedAtAsc(Long artId, Pageable pageable);
    
    Page<Comment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    Page<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId, Pageable pageable);
    
    @Query("SELECT c FROM Comment c WHERE c.art.id = :artId AND c.parentComment IS NULL ORDER BY c.createdAt ASC")
    Page<Comment> findRootCommentsByArtId(@Param("artId") Long artId, Pageable pageable);
    
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentCommentId ORDER BY c.createdAt ASC")
    Page<Comment> findRepliesByParentCommentId(@Param("parentCommentId") Long parentCommentId, Pageable pageable);
        
    // Поиск всех комментариев пользователя
    List<Comment> findByUserId(Long userId);
    
    // Поиск всех комментариев арта
    List<Comment> findByArtId(Long artId);
    
    // Поиск всех ответов на комментарий
    List<Comment> findByParentCommentId(Long parentCommentId);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.art.id = :artId")
    Long countByArtId(@Param("artId") Long artId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parentComment.id = :parentCommentId")
    Long countByParentCommentId(@Param("parentCommentId") Long parentCommentId);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    // Удаление всех комментариев пользователя
    @Modifying
    void deleteByUserId(Long userId);
    
    // Удаление всех комментариев арта
    @Modifying
    void deleteByArtId(Long artId);
    
    // Удаление всех ответов на комментарий
    @Modifying
    void deleteByParentCommentId(Long parentCommentId);
    
    // Анонимизация комментариев пользователя (при удалении аккаунта)
    @Modifying
    @Query("UPDATE Comment c SET c.text = '[Deleted comment]', c.user = null WHERE c.user.id = :userId")
    void anonymizeUserComments(@Param("userId") Long userId);
    
    // Скрытие комментариев пользователя (при бане) - ИСПРАВЛЕНО: c.hidden вместо c.isHidden
    @Modifying
    @Query("UPDATE Comment c SET c.text = '[Hidden by moderator]', c.hidden = true WHERE c.user.id = :userId")
    void hideUserComments(@Param("userId") Long userId);
}