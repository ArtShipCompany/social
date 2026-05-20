package com.example.artship.social.service;

import com.example.artship.social.model.Comment;
import com.example.artship.social.model.CommentLikes;
import com.example.artship.social.model.User;
import com.example.artship.social.repository.CommentLikesRepository;
import com.example.artship.social.repository.CommentRepository;
import com.example.artship.social.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentLikeService {
    
    private static final Logger log = LoggerFactory.getLogger(CommentLikeService.class);
    
    private final CommentLikesRepository commentLikesRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    
    public CommentLikeService(CommentLikesRepository commentLikesRepository,
                              CommentRepository commentRepository,
                              UserRepository userRepository) {
        this.commentLikesRepository = commentLikesRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }
    
    // Поставить лайк комментарию
    @Transactional
    public void likeComment(Long userId, Long commentId) {
        log.info("User {} likes comment {}", userId, commentId);
        
        if (commentLikesRepository.existsByUserIdAndCommentId(userId, commentId)) {
            throw new RuntimeException("User already liked this comment");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        CommentLikes like = new CommentLikes(user, comment);
        commentLikesRepository.save(like);
        
        log.info("User {} liked comment {}", userId, commentId);
    }
    
    // Убрать лайк с комментария
    @Transactional
    public void unlikeComment(Long userId, Long commentId) {
        log.info("User {} unlikes comment {}", userId, commentId);
        
        if (!commentLikesRepository.existsByUserIdAndCommentId(userId, commentId)) {
            throw new RuntimeException("User has not liked this comment");
        }
        
        commentLikesRepository.deleteByUserIdAndCommentId(userId, commentId);
        
        log.info("User {} unliked comment {}", userId, commentId);
    }
    
    // Проверка, лайкнул ли пользователь комментарий
    @Transactional(readOnly = true)
    public boolean isLikedByUser(Long userId, Long commentId) {
        return commentLikesRepository.existsByUserIdAndCommentId(userId, commentId);
    }
    
    // Количество лайков комментария
    @Transactional(readOnly = true)
    public long getLikesCount(Long commentId) {
        return commentLikesRepository.countByCommentId(commentId);
    }
    
    // Удаление всех лайков комментария (при удалении комментария)
    @Transactional
    public void deleteAllLikesByCommentId(Long commentId) {
        log.info("Deleting all likes for comment {}", commentId);
        commentLikesRepository.deleteByCommentId(commentId);
    }
    
    // Удаление всех лайков пользователя (при удалении аккаунта)
    @Transactional
    public void deleteAllLikesByUserId(Long userId) {
        log.info("Deleting all likes for user {}", userId);
        commentLikesRepository.deleteByUserId(userId);
    }
}
