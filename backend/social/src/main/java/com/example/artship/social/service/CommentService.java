package com.example.artship.social.service;

import com.example.artship.social.dto.CommentDto;
import com.example.artship.social.model.Art;
import com.example.artship.social.model.Comment;
import com.example.artship.social.model.User;
import com.example.artship.social.repository.ArtRepository;
import com.example.artship.social.repository.CommentRepository;
import com.example.artship.social.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentService {
    
    private static final Logger log = LoggerFactory.getLogger(CommentService.class);
    
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ArtRepository artRepository;
    
    public CommentService(CommentRepository commentRepository, UserRepository userRepository, ArtRepository artRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.artRepository = artRepository;
    }
    
    // Создание комментария
    public CommentDto createComment(String text, Long artId, Long userId, Long parentCommentId) {
        log.info("Creating comment: text='{}', artId={}, userId={}, parentCommentId={}", 
                 text, artId, userId, parentCommentId);
        
        // Валидация
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment text cannot be empty");
        }
        
        // Находим пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Находим арт
        Art art = artRepository.findById(artId)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + artId));
        
        // Создаем комментарий
        Comment comment = new Comment();
        comment.setText(text.trim());
        comment.setUser(user);
        comment.setArt(art);
        comment.setCreatedAt(LocalDateTime.now());
        
        // Обрабатываем родительский комментарий, если указан
        Comment parentComment = null;
        if (parentCommentId != null && parentCommentId > 0) {
            parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found with id: " + parentCommentId));
            
            // Дополнительная проверка: родительский комментарий должен принадлежать тому же арту
            if (!parentComment.getArt().getId().equals(artId)) {
                throw new RuntimeException("Parent comment belongs to different art");
            }
            
            comment.setParentComment(parentComment);
        }
        
        // Сохраняем
        Comment savedComment = commentRepository.save(comment);
        log.info("Comment created successfully with ID: {}", savedComment.getId());
        
        // Используем новый конструктор с передачей parentCommentId
        return new CommentDto(savedComment, parentCommentId);
    }
    
    // Получение комментария по ID
    @Transactional(readOnly = true)
    public Optional<CommentDto> getCommentById(Long id) {
        log.debug("Getting comment by ID: {}", id);
        
        return commentRepository.findById(id)
                .map(comment -> {
                    Long parentId = comment.getParentComment() != null 
                            ? comment.getParentComment().getId() 
                            : null;
                    return new CommentDto(comment, parentId);
                });
    }
    
    // Обновление комментария
    public CommentDto updateComment(Long id, String text) {
        log.info("Updating comment {} with text: {}", id, text);
        
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment text cannot be empty");
        }
        
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        
        comment.setText(text.trim());
        Comment updatedComment = commentRepository.save(comment);
        
        Long parentId = updatedComment.getParentComment() != null 
                ? updatedComment.getParentComment().getId() 
                : null;
        
        log.info("Comment {} updated successfully", id);
        return new CommentDto(updatedComment, parentId);
    }
    
    // Удаление комментария
    public void deleteComment(Long id) {
        log.info("Deleting comment with ID: {}", id);
        
        if (!commentRepository.existsById(id)) {
            throw new RuntimeException("Comment not found with id: " + id);
        }
        
        commentRepository.deleteById(id);
        log.info("Comment {} deleted successfully", id);
    }
    
    // Получение комментариев арта
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByArtId(Long artId) {
        log.debug("Getting comments for art ID: {}", artId);
        
        return commentRepository.findByArtIdOrderByCreatedAtAsc(artId).stream()
                .map(comment -> {
                    Long parentId = comment.getParentComment() != null 
                            ? comment.getParentComment().getId() 
                            : null;
                    return new CommentDto(comment, parentId);
                })
                .collect(Collectors.toList());
    }
    
    // Получение корневых комментариев арта с ответами
    @Transactional(readOnly = true)
    public List<CommentDto> getRootCommentsWithReplies(Long artId) {
        log.debug("Getting root comments with replies for art ID: {}", artId);
        
        List<Comment> rootComments = commentRepository.findRootCommentsByArtId(artId);
        
        return rootComments.stream()
                .map(rootComment -> {
                    Long parentId = rootComment.getParentComment() != null 
                            ? rootComment.getParentComment().getId() 
                            : null;
                    CommentDto dto = new CommentDto(rootComment, parentId);
                    
                    // Получаем ответы для этого корневого комментария
                    List<CommentDto> replies = getRepliesByCommentId(rootComment.getId());
                    dto.setReplies(replies);
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    // Получение ответов на комментарий
    @Transactional(readOnly = true)
    public List<CommentDto> getRepliesByCommentId(Long commentId) {
        log.debug("Getting replies for comment ID: {}", commentId);
        
        return commentRepository.findRepliesByParentCommentId(commentId).stream()
                .map(reply -> {
                    // Для ответов parentCommentId всегда равен commentId (ID родительского комментария)
                    return new CommentDto(reply, commentId);
                })
                .collect(Collectors.toList());
    }
    
    // Получение комментариев пользователя
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByUserId(Long userId) {
        log.debug("Getting comments for user ID: {}", userId);
        
        return commentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(comment -> {
                    Long parentId = comment.getParentComment() != null 
                            ? comment.getParentComment().getId() 
                            : null;
                    return new CommentDto(comment, parentId);
                })
                .collect(Collectors.toList());
    }
    
    // Количество комментариев арта
    @Transactional(readOnly = true)
    public Long getCommentCountByArtId(Long artId) {
        log.debug("Getting comment count for art ID: {}", artId);
        return commentRepository.countByArtId(artId);
    }
    
    // Количество комментариев пользователя
    @Transactional(readOnly = true)
    public Long getCommentCountByUserId(Long userId) {
        log.debug("Getting comment count for user ID: {}", userId);
        return commentRepository.countByUserId(userId);
    }
}