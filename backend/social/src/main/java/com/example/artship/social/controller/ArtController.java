package com.example.artship.social.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.artship.social.dto.ArtDto;
import com.example.artship.social.model.Art;
import com.example.artship.social.model.User;
import com.example.artship.social.model.enumclass.ArtStatus;
import com.example.artship.social.requests.CreateArtRequest;
import com.example.artship.social.requests.PrivacyUpdateRequest;
import com.example.artship.social.requests.UpdateArtRequest;
import com.example.artship.social.service.ArtService;
import com.example.artship.social.service.LocalFileStorageService;
import com.example.artship.social.service.PermissionService;
import com.example.artship.social.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/arts")
@Tag(name = "Art Controller", description = "API для управления артами")
public class ArtController {
    
    private static final Logger logger = LoggerFactory.getLogger(ArtController.class);
    
    private final ArtService artService;
    private final UserService userService;
    private final LocalFileStorageService fileStorageService;
    private final PermissionService permissionService;
    
    public ArtController(ArtService artService, 
                         UserService userService, 
                         LocalFileStorageService fileStorageService,
                         PermissionService permissionService) {
        this.artService = artService;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
        this.permissionService = permissionService;
    }
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, 
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArtDto> createArt(
            @ModelAttribute CreateArtRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        if (request.getImageFile() == null || request.getImageFile().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        String contentType = request.getImageFile().getContentType();
        if (!isValidImageFormat(contentType)) {
            return ResponseEntity.badRequest().build();
        }
        
        long fileSize = request.getImageFile().getSize();
        if (fileSize > 10 * 1024 * 1024) {
            return ResponseEntity.badRequest().build();
        }
        
        String imageUrl = fileStorageService.uploadFile(request.getImageFile());
        
        Art art = new Art();
        art.setTitle(request.getTitle().trim());
        art.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        art.setImageUrl(imageUrl);
        art.setProjectDataUrl(request.getProjectDataUrl() != null ? request.getProjectDataUrl().trim() : null);
        art.setIsPublicFlag(request.getIsPublicFlag() != null ? request.getIsPublicFlag() : true);
        
        ArtDto createdArt = artService.createArt(art, currentUser.getId());
        
        return new ResponseEntity<>(createdArt, HttpStatus.CREATED);
    }
        
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
               produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArtDto> updateArt(
            @PathVariable Long id,
            @ModelAttribute UpdateArtRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        
        Optional<Art> artOpt = artService.getArtById(id);
        if (artOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Art existingArt = artOpt.get();
        
        if (!permissionService.canEditContent(currentUser, existingArt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            existingArt.setTitle(request.getTitle().trim());
        }
        
        if (request.getDescription() != null) {
            existingArt.setDescription(request.getDescription().trim());
        }
        
        if (request.getProjectDataUrl() != null) {
            existingArt.setProjectDataUrl(request.getProjectDataUrl().trim());
        }
        
        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            String contentType = request.getImageFile().getContentType();
            if (!isValidImageFormat(contentType)) {
                return ResponseEntity.badRequest().build();
            }
            
            long fileSize = request.getImageFile().getSize();
            if (fileSize > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().build();
            }
            
            String newImageUrl = fileStorageService.uploadFile(request.getImageFile());
            
            try {
                fileStorageService.deleteFile(existingArt.getImageUrl());
            } catch (Exception e) {
                logger.warn("Не удалось удалить старое изображение: {}", e.getMessage());
            }
            
            existingArt.setImageUrl(newImageUrl);
        }
        
        existingArt.setUpdatedAt(LocalDateTime.now());
        
        ArtDto updatedArt = artService.updateArt(id, existingArt);
        
        return ResponseEntity.ok(updatedArt);
    }
    
    @PatchMapping(value = "/{id}/privacy", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArtDto> updateArtPrivacy(
            @PathVariable Long id,
            @ModelAttribute PrivacyUpdateRequest privacyRequest,
            @AuthenticationPrincipal UserDetails userDetails) {  
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        
        if (privacyRequest.getIsPublicFlag() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Optional<Art> artOpt = artService.getArtById(id);
        if (artOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Art art = artOpt.get();
        
        if (!permissionService.canManageArt(currentUser, art)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        art.setIsPublicFlag(privacyRequest.getIsPublicFlag());
        art.setUpdatedAt(LocalDateTime.now());
        
        Art updatedArt = artService.save(art);
        ArtDto result = artService.convertToDto(updatedArt);
        
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArt(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        
        Optional<Art> artOpt = artService.getArtById(id);
        if (artOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Art art = artOpt.get();
        
        if (!permissionService.canManageArt(currentUser, art)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            fileStorageService.deleteFile(art.getImageUrl());
        } catch (Exception e) {
            logger.warn("Не удалось удалить файл изображения: {}", e.getMessage());
        }
        
        artService.deleteArt(id);
        
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/public")
    public ResponseEntity<Page<ArtDto>> getPublicArts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Sort sort = direction.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        User currentUser = null;
        if (userDetails != null) {
            Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
            currentUser = userOpt.orElse(null);
        }
        
        Page<ArtDto> arts = artService.getPublicArtsDtos(pageable, currentUser);
        
        return ResponseEntity.ok(arts);
    }
    
    @GetMapping("/feed")
    public ResponseEntity<Page<ArtDto>> getUserFeed(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<ArtDto> feed = artService.getUserFeedDtos(currentUser.getId(), pageable, currentUser);
        
        return ResponseEntity.ok(feed);
    }
    
    @GetMapping("/author/{userId}")
    public ResponseEntity<Page<ArtDto>> getPublicArtsByAuthor(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Optional<User> author = userService.findById(userId);
        if (author.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User currentUser = null;
        if (userDetails != null) {
            Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
            currentUser = userOpt.orElse(null);
        }
        
        Page<ArtDto> arts = artService.getPublicArtDtosByAuthor(author.get(), pageable, currentUser);
        
        return ResponseEntity.ok(arts);
    }
    
    @GetMapping("/my-arts")
    public ResponseEntity<Page<ArtDto>> getMyArts(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = userOpt.get();
        
        Page<ArtDto> arts = artService.getAllArtDtosByAuthor(currentUser, pageable, currentUser);
        
        return ResponseEntity.ok(arts);
    }
    
    @GetMapping("/admin/by-status")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<Page<ArtDto>> getArtsByStatus(
            @RequestParam(required = false) ArtStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        
        Page<ArtDto> arts = artService.getArtsByStatus(status, pageable);
        
        return ResponseEntity.ok(arts);
    }
    
    @GetMapping("/admin/status-statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<ArtStatus, Long>> getArtsStatusStatistics() {
        Map<ArtStatus, Long> statistics = artService.getArtsStatusStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ArtDto> getArtById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Optional<Art> artOpt = artService.getArtById(id);
        if (artOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Art art = artOpt.get();
        
        User currentUser = null;
        if (userDetails != null) {
            Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
            currentUser = userOpt.orElse(null);
        }
        
        if (!permissionService.canViewArt(currentUser, art)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        ArtDto artDto = artService.convertToDto(art);
        return ResponseEntity.ok(artDto);
    }
    
    @GetMapping("/{id}/access")
    public ResponseEntity<Boolean> checkArtAccess(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Optional<Art> artOpt = artService.getArtById(id);
        if (artOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Art art = artOpt.get();
        
        User currentUser = null;
        if (userDetails != null) {
            Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
            currentUser = userOpt.orElse(null);
        }
        
        boolean hasAccess = permissionService.canViewArt(currentUser, art);
        
        return ResponseEntity.ok(hasAccess);
    }

    @PatchMapping("/{artId}/hide")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<ArtDto> hideArt(
            @PathVariable Long artId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            ArtDto updatedArt = artService.hideArt(artId);
            logger.info("Арт {} скрыт пользователем {}", artId, userDetails.getUsername());
            return ResponseEntity.ok(updatedArt);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/{artId}/unhide")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<ArtDto> unhideArt(
            @PathVariable Long artId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            ArtDto updatedArt = artService.unhideArt(artId);
            logger.info("Арт {} восстановлен пользователем {}", artId, userDetails.getUsername());
            return ResponseEntity.ok(updatedArt);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/{artId}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtDto> banArt(
            @PathVariable Long artId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            ArtDto updatedArt = artService.banArt(artId);
            logger.info("Арт {} забанен администратором {}", artId, userDetails.getUsername());
            return ResponseEntity.ok(updatedArt);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{artId}/force")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> forceDeleteArt(
            @PathVariable Long artId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            artService.forceDeleteArt(artId);
            logger.info("Арт {} принудительно удалён администратором {}", artId, userDetails.getUsername());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    private Sort parseSort(String sortParam) {
        String[] sortParts = sortParam.split(",");
        if (sortParts.length == 2) {
            String field = sortParts[0];
            String direction = sortParts[1];
            return "desc".equalsIgnoreCase(direction) 
                ? Sort.by(field).descending() 
                : Sort.by(field).ascending();
        }
        return Sort.by("createdAt").descending();
    }
    
    private boolean isValidImageFormat(String contentType) {
        if (contentType == null) {
            return false;
        }
        
        return contentType.equals("image/jpeg") || 
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp") ||
               contentType.equals("image/svg+xml") ||
               contentType.equals("image/bmp");
    }
}