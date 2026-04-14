package com.example.artship.social.controller;

import com.example.artship.social.dto.CommentDto;
import com.example.artship.social.model.Comment;
import com.example.artship.social.model.User;
import com.example.artship.social.requests.CommentRequest;
import com.example.artship.social.service.CommentService;
import com.example.artship.social.service.PermissionService;
import com.example.artship.social.service.UserService;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);
    
    private final CommentService commentService;
    private final UserService userService;
    private final PermissionService permissionService;
    
    public CommentController(CommentService commentService, 
                             UserService userService,
                             PermissionService permissionService) {
        this.commentService = commentService;
        this.userService = userService;
        this.permissionService = permissionService;
    }
    
    // Создание комментария (с проверкой авторизации)
    @Operation(summary = "Создать комментарий")
    @PostMapping
    public ResponseEntity<?> createComment(
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("=== СОЗДАНИЕ КОММЕНТАРИЯ ===");
        
        // Проверка авторизации
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Получение пользователя
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        
        try {
            CommentDto comment = commentService.createComment(
                request.getText(), 
                request.getArtId(), 
                currentUser.getId(),  
                request.getParentCommentId()
            );
            
            URI location = URI.create("/api/comments/" + comment.getId());
            
            logger.info("Комментарий создан пользователем {} с ID: {}", 
                       currentUser.getUsername(), comment.getId());
            return ResponseEntity.created(location).body(comment);
            
        } catch (RuntimeException e) {
            logger.error("Ошибка создания комментария: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
        
    // Получение комментария по ID
    @Operation(summary = "Получить комментарий по ID")
    @GetMapping("/{id}")
    public ResponseEntity<CommentDto> getComment(@PathVariable Long id) {
        logger.debug("Получение комментария по ID: {}", id);
        
        Optional<CommentDto> comment = commentService.getCommentById(id);
        return comment.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    // Корневые комментарии арта с ответами (с пагинацией)
    @Operation(summary = "Получить корневые комментарии арта с ответами")
    @GetMapping("/art/{artId}/root")
    public ResponseEntity<Page<CommentDto>> getRootCommentsWithReplies(
            @PathVariable Long artId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<CommentDto> comments = commentService.getRootCommentsWithReplies(artId, pageable);
        return ResponseEntity.ok(comments);
    }
    
    // Ответы на комментарий (с пагинацией)
    @Operation(summary = "Получить ответы на комментарий")
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<Page<CommentDto>> getReplies(
            @PathVariable Long commentId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CommentDto> replies = commentService.getRepliesByCommentId(commentId, pageable);
        return ResponseEntity.ok(replies);
    }
    
    // Комментарии пользователя (с пагинацией)
    @Operation(summary = "Получить комментарии пользователя")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<CommentDto>> getCommentsByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CommentDto> comments = commentService.getCommentsByUserId(userId, pageable);
        return ResponseEntity.ok(comments);
    }
    
    // Количество комментариев арта
    @Operation(summary = "Получить количество комментариев арта")
    @GetMapping("/art/{artId}/count")
    public ResponseEntity<Long> getCommentCountByArt(@PathVariable Long artId) {
        Long count = commentService.getCommentCountByArtId(artId);
        return ResponseEntity.ok(count);
    }
    
    // Количество комментариев пользователя
    @Operation(summary = "Получить количество комментариев пользователя")
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> getCommentCountByUser(@PathVariable Long userId) {
        Long count = commentService.getCommentCountByUserId(userId);
        return ResponseEntity.ok(count);
    }

    
    
    // Удаление комментария 
    @Operation(summary = "Удалить комментарий (автор, модератор или администратор)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        logger.info("=== УДАЛЕНИЕ КОММЕНТАРИЯ ID: {} ===", id);
        
        if (userDetails == null) {
            logger.error("Пользователь не авторизован");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            logger.error("Пользователь не найден: {}", userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        
        Optional<Comment> commentOpt = commentService.getCommentEntityById(id);
        if (commentOpt.isEmpty()) {
            logger.warn("Комментарий с ID {} не найден", id);
            return ResponseEntity.notFound().build();
        }
        
        Comment comment = commentOpt.get();

        boolean canDelete = false;
        
        if (comment.getUser().getId().equals(currentUser.getId())) {
            canDelete = true;
            logger.info("Автор удаляет свой комментарий");
        }
        else if (permissionService.isModerator(currentUser)) {
            canDelete = true;
            logger.info("Модератор {} удаляет комментарий пользователя {}", 
                    currentUser.getUsername(), comment.getUser().getUsername());
        }
        else if (permissionService.isAdmin(currentUser)) {
            canDelete = true;
            logger.info("Администратор {} удаляет комментарий пользователя {}", 
                    currentUser.getUsername(), comment.getUser().getUsername());
        }
        
        if (!canDelete) {
            logger.warn("Пользователь {} не имеет прав на удаление комментария {}", 
                    currentUser.getUsername(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            commentService.deleteComment(id);
            logger.info("Комментарий {} удален пользователем {}", id, currentUser.getUsername());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Ошибка удаления комментария: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}