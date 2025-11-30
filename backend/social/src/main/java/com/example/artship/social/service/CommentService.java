package com.example.artship.social.service;

import com.example.artship.social.dto.CommentDto;
import com.example.artship.social.model.Art;
import com.example.artship.social.model.Comment;
import com.example.artship.social.model.User;
import com.example.artship.social.repository.ArtRepository;
import com.example.artship.social.repository.CommentRepository;
import com.example.artship.social.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentService {
    
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Art art = artRepository.findById(artId)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + artId));
        
        Comment comment = new Comment(text, art, user);
        
        if (parentCommentId != null) {
            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found with id: " + parentCommentId));
            comment.setParentComment(parentComment);
        }
        
        Comment savedComment = commentRepository.save(comment);
        return new CommentDto(savedComment);
    }
    
    // Получение комментария по ID
    @Transactional(readOnly = true)
    public Optional<CommentDto> getCommentById(Long id) {
        return commentRepository.findById(id)
                .map(CommentDto::new);
    }
    
    // Обновление комментария
    public CommentDto updateComment(Long id, String text) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        
        comment.setText(text);
        Comment updatedComment = commentRepository.save(comment);
        return new CommentDto(updatedComment);
    }
    
    // Удаление комментария
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
        commentRepository.delete(comment);
    }
    
    // Получение комментариев арта
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByArtId(Long artId) {
        return commentRepository.findByArtIdOrderByCreatedAtAsc(artId).stream()
                .map(CommentDto::new)
                .collect(Collectors.toList());
    }
    
    // Получение корневых комментариев арта с ответами
    @Transactional(readOnly = true)
    public List<CommentDto> getRootCommentsWithReplies(Long artId) {
        List<Comment> rootComments = commentRepository.findRootCommentsByArtId(artId);
        return rootComments.stream()
                .map(comment -> {
                    CommentDto dto = new CommentDto(comment);
                    List<CommentDto> replies = getRepliesByCommentId(comment.getId());
                    dto.setReplies(replies);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    // Получение ответов на комментарий
    @Transactional(readOnly = true)
    public List<CommentDto> getRepliesByCommentId(Long commentId) {
        return commentRepository.findRepliesByParentCommentId(commentId).stream()
                .map(CommentDto::new)
                .collect(Collectors.toList());
    }
    
    // Получение комментариев пользователя
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByUserId(Long userId) {
        return commentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(CommentDto::new)
                .collect(Collectors.toList());
    }
    
    // Количество комментариев арта
    @Transactional(readOnly = true)
    public Long getCommentCountByArtId(Long artId) {
        return commentRepository.countByArtId(artId);
    }
    
    // Количество комментариев пользователя
    @Transactional(readOnly = true)
    public Long getCommentCountByUserId(Long userId) {
        return commentRepository.countByUserId(userId);
    }
}