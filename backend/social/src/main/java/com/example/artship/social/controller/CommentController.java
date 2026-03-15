package com.example.artship.social.controller;

import com.example.artship.social.dto.CommentDto;
import com.example.artship.social.requests.CommentRequest;
import com.example.artship.social.requests.CommentUpdateRequest;
import com.example.artship.social.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    
    private static final Logger log = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;
    
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }
    
    // Создание комментария
    @PostMapping
    public ResponseEntity<?> createComment(@RequestBody CommentRequest request) {
        log.info("Creating comment request: {}", request);
        
        try {
            CommentDto comment = commentService.createComment(
                request.getText(), 
                request.getArtId(), 
                request.getUserId(), 
                request.getParentCommentId()
            );
            
            URI location = URI.create("/api/comments/" + comment.getId());
            
            log.info("Comment created successfully with ID: {}", comment.getId());
            return ResponseEntity.created(location).body(comment);
            
        } catch (RuntimeException e) {
            log.error("Error creating comment: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Получение комментария по ID
    @GetMapping("/{id}")
    public ResponseEntity<CommentDto> getComment(@PathVariable Long id) {
        log.debug("Getting comment by ID: {}", id);
        
        Optional<CommentDto> comment = commentService.getCommentById(id);
        return comment.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    // Обновление комментария
    @PutMapping("/{id}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long id,
            @RequestBody CommentUpdateRequest request) {
        log.info("Updating comment {} with request: {}", id, request);
        
        try {
            CommentDto comment = commentService.updateComment(id, request.getText());
            return ResponseEntity.ok(comment);
        } catch (RuntimeException e) {
            log.error("Error updating comment {}: ", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    // Удаление комментария
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        try {
            commentService.deleteComment(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Корневые комментарии арта с ответами (с пагинацией)
    @GetMapping("/art/{artId}/root")
    public ResponseEntity<Page<CommentDto>> getRootCommentsWithReplies(
            @PathVariable Long artId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<CommentDto> comments = commentService.getRootCommentsWithReplies(artId, pageable);
        return ResponseEntity.ok(comments);
    }
    
    // Ответы на комментарий (с пагинацией)
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<Page<CommentDto>> getReplies(
            @PathVariable Long commentId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CommentDto> replies = commentService.getRepliesByCommentId(commentId, pageable);
        return ResponseEntity.ok(replies);
    }
    
    // Комментарии пользователя (с пагинацией)
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<CommentDto>> getCommentsByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CommentDto> comments = commentService.getCommentsByUserId(userId, pageable);
        return ResponseEntity.ok(comments);
    }
    
    // Количество комментариев арта
    @GetMapping("/art/{artId}/count")
    public ResponseEntity<Long> getCommentCountByArt(@PathVariable Long artId) {
        Long count = commentService.getCommentCountByArtId(artId);
        return ResponseEntity.ok(count);
    }
    
    // Количество комментариев пользователя
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> getCommentCountByUser(@PathVariable Long userId) {
        Long count = commentService.getCommentCountByUserId(userId);
        return ResponseEntity.ok(count);
    }
    
    
}