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
    private final CommentLikeService commentLikeService;  // ← Добавить
    
    public CommentService(CommentRepository commentRepository, 
                         UserRepository userRepository, 
                         ArtRepository artRepository,
                         CommentLikeService commentLikeService) {  // ← Добавить
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.artRepository = artRepository;
        this.commentLikeService = commentLikeService;
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
        
        if (parentCommentId != null && parentCommentId > 0) {
            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found with id: " + parentCommentId));
            
            if (!parentComment.getArt().getId().equals(artId)) {
                throw new RuntimeException("Parent comment belongs to different art");
            }
            
            comment.setParentComment(parentComment);
        }
        
        Comment savedComment = commentRepository.save(comment);
        log.info("Comment created successfully with ID: {}", savedComment.getId());
        
        return convertToDto(savedComment, null);
    }
    
    // Получение комментария по ID (с лайками)
    @Transactional(readOnly = true)
    public Optional<CommentDto> getCommentById(Long id, Long currentUserId) {
        log.debug("Getting comment by ID: {}", id);
        
        return commentRepository.findById(id)
                .map(comment -> convertToDto(comment, currentUserId));
    }
    
    // Получение комментария по ID (без лайков - для обратной совместимости)
    @Transactional(readOnly = true)
    public Optional<CommentDto> getCommentById(Long id) {
        return getCommentById(id, null);
    }
    
    // Обновление комментария
    public CommentDto updateComment(Long id, String text, Long currentUserId) {
        log.info("Updating comment {} with text: {}", id, text);
        
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment text cannot be empty");
        }
        
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        
        // Проверка прав: только автор может редактировать
        if (!comment.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("You don't have permission to edit this comment");
        }
        
        comment.setText(text.trim());
        Comment updatedComment = commentRepository.save(comment);
        
        log.info("Comment {} updated successfully", id);
        return convertToDto(updatedComment, currentUserId);
    }
    
    // Удаление комментария
    public void deleteComment(Long id) {
        log.info("Deleting comment with ID: {}", id);
        
        if (!commentRepository.existsById(id)) {
            throw new RuntimeException("Comment not found with id: " + id);
        }
        
        // Удаляем лайки комментария
        commentLikeService.deleteAllLikesByCommentId(id);
        
        commentRepository.deleteById(id);
        log.info("Comment {} deleted successfully", id);
    }
    
    // Получение корневых комментариев арта с ответами (с пагинацией)
    @Transactional(readOnly = true)
    public Page<CommentDto> getRootCommentsWithReplies(Long artId, Pageable pageable, Long currentUserId) {
        log.debug("Getting root comments with replies for art ID: {} with pagination: page={}, size={}", 
                 artId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Comment> rootCommentsPage = commentRepository.findRootCommentsByArtId(artId, pageable);
        
        List<CommentDto> dtos = rootCommentsPage.getContent().stream()
                .map(rootComment -> {
                    CommentDto dto = convertToDto(rootComment, currentUserId);
                    
                    Pageable repliesPageable = PageRequest.of(0, 3); 
                    Page<CommentDto> replies = getRepliesByCommentId(rootComment.getId(), repliesPageable, currentUserId);
                    dto.setReplies(replies.getContent());
                    dto.setTotalReplies(commentRepository.countByParentCommentId(rootComment.getId()));
                    dto.setReplyCount(replies.getContent().size());
                    
                    return dto;
                })
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, rootCommentsPage.getTotalElements());
    }
    
    // Получение ответов на комментарий (с пагинацией)
    @Transactional(readOnly = true)
    public Page<CommentDto> getRepliesByCommentId(Long commentId, Pageable pageable, Long currentUserId) {
        log.debug("Getting replies for comment ID: {} with pagination: page={}, size={}", 
                 commentId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Comment> repliesPage = commentRepository.findRepliesByParentCommentId(commentId, pageable);
        
        List<CommentDto> dtos = repliesPage.getContent().stream()
                .map(reply -> convertToDto(reply, currentUserId))
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, repliesPage.getTotalElements());
    }
    
    // Получение комментариев пользователя (с пагинацией)
    @Transactional(readOnly = true)
    public Page<CommentDto> getCommentsByUserId(Long userId, Pageable pageable, Long currentUserId) {
        log.debug("Getting comments for user ID: {} with pagination: page={}, size={}", 
                 userId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Comment> commentsPage = commentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        List<CommentDto> dtos = commentsPage.getContent().stream()
                .map(comment -> convertToDto(comment, currentUserId))
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, commentsPage.getTotalElements());
    }
    
    @Transactional(readOnly = true)
    public Long getCommentCountByArtId(Long artId) {
        log.debug("Getting comment count for art ID: {}", artId);
        return commentRepository.countByArtId(artId);
    }
    
    @Transactional(readOnly = true)
    public Long getCommentCountByUserId(Long userId) {
        log.debug("Getting comment count for user ID: {}", userId);
        return commentRepository.countByUserId(userId);
    }

    @Transactional
    public void deleteAllCommentsByArtId(Long artId) {
        List<Comment> comments = commentRepository.findByArtId(artId);
        
        for (Comment comment : comments) {
            commentLikeService.deleteAllLikesByCommentId(comment.getId());
        }
        
        commentRepository.deleteByArtId(artId);
    }

    @Transactional
    public void deleteAllUserComments(Long userId) {
        // Сначала удаляем лайки комментариев пользователя
        List<Comment> comments = commentRepository.findByUserId(userId);
        for (Comment comment : comments) {
            commentLikeService.deleteAllLikesByCommentId(comment.getId());
        }
        commentRepository.deleteByUserId(userId);
    }
        
    // Дополнительный метод для подсчета ответов
    @Transactional(readOnly = true)
    public Long getReplyCountByCommentId(Long commentId) {
        return commentRepository.countByParentCommentId(commentId);
    }

    @Transactional
    public void hideComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
        
        comment.setText("[Hidden by moderators]");
        comment.setHidden(true);
        
        commentRepository.save(comment);
    }

    @Transactional
    public void unhideComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
        
        comment.setHidden(false);
        
        commentRepository.save(comment);
    }
    
    // Конвертация Comment в CommentDto с информацией о лайках
    private CommentDto convertToDto(Comment comment, Long currentUserId) {
        long likesCount = commentLikeService.getLikesCount(comment.getId());
        boolean isLiked = currentUserId != null && 
                          commentLikeService.isLikedByUser(currentUserId, comment.getId());
        
        CommentDto dto = new CommentDto(comment);
        dto.setLikesCount(likesCount);
        dto.setLikedByCurrentUser(isLiked);
        
        Long parentId = comment.getParentComment() != null 
                ? comment.getParentComment().getId() 
                : null;
        dto.setParentCommentId(parentId);
        
        return dto;
    }
}