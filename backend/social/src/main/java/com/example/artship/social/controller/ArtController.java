package com.example.artship.social.controller;

import com.example.artship.social.dto.ArtDto;
import com.example.artship.social.dto.CreateArtRequest;
import com.example.artship.social.model.Art;
import com.example.artship.social.model.User;
import com.example.artship.social.service.ArtService;
import com.example.artship.social.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/arts")
public class ArtController {
    
    private final ArtService artService;
    private final UserService userService;
    
    public ArtController(ArtService artService, UserService userService) {
        this.artService = artService;
        this.userService = userService;
    }

    // Создание арта с готовой ссылкой на изображение
    @PostMapping
    public ResponseEntity<ArtDto> createArt(@RequestBody CreateArtRequest request) {
        try {
            // Валидация
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            if (request.getAuthorId() == null) {
                return ResponseEntity.badRequest().build();
            }
            
            if (request.getImage() == null || request.getImage().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            
            // Проверяем существование пользователя
            Optional<User> user = userService.findById(request.getAuthorId());
            if (user.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Создаем объект Art
            Art art = new Art();
            art.setTitle(request.getTitle());
            art.setDescription(request.getDescription());
            art.setImageUrl(request.getImage()); // Используем готовую ссылку
            art.setProjectDataUrl(request.getProjectDataUrl());
            art.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : true);
            
            ArtDto createdArt = artService.createArt(art, request.getAuthorId());
            return new ResponseEntity<>(createdArt, HttpStatus.CREATED);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<ArtDto> createArtForUser(
            @PathVariable Long userId,
            @RequestBody Art art) {
        
        ArtDto createdArt = artService.createArt(art, userId);
        return new ResponseEntity<>(createdArt, HttpStatus.CREATED);
    }

    // Получение арта по ID
    @GetMapping("/{id}")
    public ResponseEntity<ArtDto> getArtById(@PathVariable Long id) {
        Optional<ArtDto> art = artService.getArtDtoById(id);
        return art.map(ResponseEntity::ok)
                 .orElse(ResponseEntity.notFound().build());
    }

    // Обновление арта
    @PutMapping("/{id}")
    public ResponseEntity<ArtDto> updateArt(
            @PathVariable Long id,
            @RequestBody Art artDetails,
            @AuthenticationPrincipal User currentUser) {
        
        if (!artService.isUserAuthorOfArt(id, currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        ArtDto updatedArt = artService.updateArt(id, artDetails);
        return ResponseEntity.ok(updatedArt);
    }

    // Удаление арта
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArt(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        if (!artService.isUserAuthorOfArt(id, currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        artService.deleteArt(id);
        return ResponseEntity.noContent().build();
    }

    // Получение публичных артов (пагинация)
    @GetMapping("/public")
    public ResponseEntity<Page<ArtDto>> getPublicArts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort sort = direction.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ArtDto> arts = artService.getPublicArtsDtos(pageable);
        return ResponseEntity.ok(arts);
    }

    // Лента пользователя (арты тех, на кого подписан)
    @GetMapping("/feed")
    public ResponseEntity<Page<ArtDto>> getUserFeed(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ArtDto> feed = artService.getUserFeedDtos(currentUser.getId(), pageable);
        return ResponseEntity.ok(feed);
    }

    // Получение публичных артов конкретного пользователя
    @GetMapping("/author/{userId}")
    public ResponseEntity<List<ArtDto>> getPublicArtsByAuthor(@PathVariable Long userId) {
        Optional<User> author = userService.findById(userId);
        if (author.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<ArtDto> arts = artService.getPublicArtDtosByAuthor(author.get());
        return ResponseEntity.ok(arts);
    }

    // Получение всех артов текущего пользователя (включая приватные)
    @GetMapping("/my-arts")
    public ResponseEntity<List<ArtDto>> getMyArts(@AuthenticationPrincipal User currentUser) {
        List<ArtDto> arts = artService.getAllArtDtosByAuthor(currentUser);
        return ResponseEntity.ok(arts);
    }

    // Поиск публичных артов по названию
    @GetMapping("/search")
    public ResponseEntity<Page<ArtDto>> searchPublicArts(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ArtDto> arts = artService.searchPublicArtDtosByTitle(title, pageable);
        return ResponseEntity.ok(arts);
    }

    // Поиск публичных артов по тегу
    @GetMapping("/tag/{tagName}")
    public ResponseEntity<Page<ArtDto>> getArtsByTag(
            @PathVariable String tagName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ArtDto> arts = artService.findDtosByTagName(tagName, pageable);
        return ResponseEntity.ok(arts);
    }

    // Проверка прав доступа к арту
    @GetMapping("/{id}/access")
    public ResponseEntity<Boolean> checkArtAccess(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        Optional<Art> art = artService.getArtById(id);
        if (art.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Art artEntity = art.get();
        boolean hasAccess = artEntity.getIsPublic() || 
                           artEntity.getAuthor().getId().equals(currentUser.getId());
        
        return ResponseEntity.ok(hasAccess);
    }
}