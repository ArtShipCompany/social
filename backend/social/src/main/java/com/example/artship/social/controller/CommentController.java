package com.example.artship.social.controller;

import com.example.artship.social.dto.CommentDto;
import com.example.artship.social.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    
    private final CommentService commentService;
    
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }
    
    // Создание комментария
    @PostMapping
    public ResponseEntity<CommentDto> createComment(@RequestBody CommentRequest request) {
        try {
            CommentDto comment = commentService.createComment(
                request.getText(), 
                request.getArtId(), 
                request.getUserId(), 
                request.getParentCommentId()
            );
            return ResponseEntity.ok(comment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Получение комментария по ID
    @GetMapping("/{id}")
    public ResponseEntity<CommentDto> getComment(@PathVariable Long id) {
        Optional<CommentDto> comment = commentService.getCommentById(id);
        return comment.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    // Обновление комментария
    @PutMapping("/{id}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable Long id,
            @RequestBody CommentUpdateRequest request) {
        try {
            CommentDto comment = commentService.updateComment(id, request.getText());
            return ResponseEntity.ok(comment);
        } catch (RuntimeException e) {
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
    
    // Комментарии арта
    @GetMapping("/art/{artId}")
    public ResponseEntity<List<CommentDto>> getCommentsByArt(@PathVariable Long artId) {
        List<CommentDto> comments = commentService.getCommentsByArtId(artId);
        return ResponseEntity.ok(comments);
    }
    
    // Корневые комментарии арта с ответами
    @GetMapping("/art/{artId}/root")
    public ResponseEntity<List<CommentDto>> getRootCommentsWithReplies(@PathVariable Long artId) {
        List<CommentDto> comments = commentService.getRootCommentsWithReplies(artId);
        return ResponseEntity.ok(comments);
    }
    
    // Ответы на комментарий
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<List<CommentDto>> getReplies(@PathVariable Long commentId) {
        List<CommentDto> replies = commentService.getRepliesByCommentId(commentId);
        return ResponseEntity.ok(replies);
    }
    
    // Комментарии пользователя
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentDto>> getCommentsByUser(@PathVariable Long userId) {
        List<CommentDto> comments = commentService.getCommentsByUserId(userId);
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
    
    // DTO для создания комментария
    public static class CommentRequest {
        private String text;
        private Long artId;
        private Long userId;
        private Long parentCommentId;
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public Long getArtId() { return artId; }
        public void setArtId(Long artId) { this.artId = artId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getParentCommentId() { return parentCommentId; }
        public void setParentCommentId(Long parentCommentId) { this.parentCommentId = parentCommentId; }
    }
    
    // DTO для обновления комментария
    public static class CommentUpdateRequest {
        private String text;
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}