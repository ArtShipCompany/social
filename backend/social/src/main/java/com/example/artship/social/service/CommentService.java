package com.example.artship.social.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.artship.social.dto.CommentDto;
import com.example.artship.social.model.Art;
import com.example.artship.social.model.Comment;
import com.example.artship.social.model.User;
import com.example.artship.social.repository.ArtRepository;
import com.example.artship.social.repository.CommentRepository;
import com.example.artship.social.repository.UserRepository;

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

    public Optional<Comment> getCommentEntityById(Long id) {
        return commentRepository.findById(id);
    }
    
    // Создание комментария
    public CommentDto createComment(String text, Long artId, Long userId, Long parentCommentId) {
        log.info("Creating comment: text='{}', artId={}, userId={}, parentCommentId={}", 
                 text, artId, userId, parentCommentId);

        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment text cannot be empty");
        }
        

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
   
        Art art = artRepository.findById(artId)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + artId));

        Comment comment = new Comment();
        comment.setText(text.trim());
        comment.setUser(user);
        comment.setArt(art);
        comment.setCreatedAt(LocalDateTime.now());
        
        Comment parentComment = null;
        if (parentCommentId != null && parentCommentId > 0) {
            parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found with id: " + parentCommentId));
            
            if (!parentComment.getArt().getId().equals(artId)) {
                throw new RuntimeException("Parent comment belongs to different art");
            }
            
            comment.setParentComment(parentComment);
        }
        
        Comment savedComment = commentRepository.save(comment);
        log.info("Comment created successfully with ID: {}", savedComment.getId());
        
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
    
    // Получение корневых комментариев арта с ответами (с пагинацией)
    @Transactional(readOnly = true)
    public Page<CommentDto> getRootCommentsWithReplies(Long artId, Pageable pageable) {
        log.debug("Getting root comments with replies for art ID: {} with pagination: page={}, size={}", 
                 artId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Comment> rootCommentsPage = commentRepository.findRootCommentsByArtId(artId, pageable);
        
        List<CommentDto> dtos = rootCommentsPage.getContent().stream()
                .map(rootComment -> {
                    Long parentId = rootComment.getParentComment() != null 
                            ? rootComment.getParentComment().getId() 
                            : null;
                    CommentDto dto = new CommentDto(rootComment, parentId);
                    
                    // Получаем ТОЛЬКО ПЕРВЫЕ НЕСКОЛЬКО ответов для предпросмотра
                    Pageable repliesPageable = PageRequest.of(0, 3); // Показываем только первые 3 ответа
                    Page<CommentDto> replies = getRepliesByCommentId(rootComment.getId(), repliesPageable);
                    dto.setReplies(replies.getContent());
                    dto.setTotalReplies(commentRepository.countByParentCommentId(rootComment.getId()));
                    
                    return dto;
                })
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, rootCommentsPage.getTotalElements());
    }
    
    // Получение ответов на комментарий (с пагинацией)
    @Transactional(readOnly = true)
    public Page<CommentDto> getRepliesByCommentId(Long commentId, Pageable pageable) {
        log.debug("Getting replies for comment ID: {} with pagination: page={}, size={}", 
                 commentId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Comment> repliesPage = commentRepository.findRepliesByParentCommentId(commentId, pageable);
        
        List<CommentDto> dtos = repliesPage.getContent().stream()
                .map(reply -> new CommentDto(reply, commentId))
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, repliesPage.getTotalElements());
    }
    
    // Получение комментариев пользователя (с пагинацией)
    @Transactional(readOnly = true)
    public Page<CommentDto> getCommentsByUserId(Long userId, Pageable pageable) {
        log.debug("Getting comments for user ID: {} with pagination: page={}, size={}", 
                 userId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Comment> commentsPage = commentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        List<CommentDto> dtos = commentsPage.getContent().stream()
                .map(comment -> {
                    Long parentId = comment.getParentComment() != null 
                            ? comment.getParentComment().getId() 
                            : null;
                    return new CommentDto(comment, parentId);
                })
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, commentsPage.getTotalElements());
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
    
    // Дополнительный метод для подсчета ответов
    @Transactional(readOnly = true)
    public Long getReplyCountByCommentId(Long commentId) {
        return commentRepository.countByParentCommentId(commentId);
    }
}