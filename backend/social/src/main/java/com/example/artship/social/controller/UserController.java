package com.example.artship.social.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.artship.social.dto.UserDto;
import com.example.artship.social.requests.UserUpdateRequest;
import com.example.artship.social.model.User;
import com.example.artship.social.service.UserService;
import com.example.artship.social.service.LocalFileStorageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private LocalFileStorageService fileStorageService;
    
    @GetMapping("/public/{id}")
    public ResponseEntity<UserDto> getPublicUser(@PathVariable Long id) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isPresent() && userOptional.get().getIsPublic()) {
            return ResponseEntity.ok(new UserDto(userOptional.get()));
        }
        return ResponseEntity.notFound().build();
    }
    

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isPresent()) {
            return ResponseEntity.ok(new UserDto(userOptional.get()));
        }
        return ResponseEntity.notFound().build();
    }
    
    // Вариант 1: Multipart/form-data с файлом + JSON данными
    @PutMapping(value = "/me", consumes = {"multipart/form-data"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> updateCurrentUserWithAvatar(
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            @RequestParam(value = "displayName", required = false) String displayName,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "isPublic", required = false) Boolean isPublic) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findByUsername(username);
        if (!userOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        User existingUser = userOptional.get();
        
        // Обработка аватарки если загружен файл
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                // Проверяем тип файла
                String contentType = avatarFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.badRequest().body(null);
                }
                
                // Сохраняем файл
                String newAvatarUrl = fileStorageService.uploadFile(avatarFile);
                System.out.println("Avatar uploaded in multipart PUT: " + newAvatarUrl);
                
                // Удаляем старую аватарку если она была
                deleteOldAvatar(existingUser.getAvatarUrl());
                
                // Обновляем URL аватарки
                existingUser.setAvatarUrl(newAvatarUrl);
            } catch (Exception e) {
                System.err.println("Error uploading avatar in PUT: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        }
        
        // Обновляем остальные поля
        if (displayName != null) {
            existingUser.setDisplayName(displayName);
        }
        if (bio != null) {
            existingUser.setBio(bio);
        }
        if (isPublic != null) {
            existingUser.setIsPublic(isPublic);
        }
        
        User updatedUser = userService.save(existingUser);
        return ResponseEntity.ok(new UserDto(updatedUser));
    }
    
    // Вариант 2: JSON запрос с URL аватарки
    @PutMapping(value = "/me", consumes = {"application/json"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> updateCurrentUser(@RequestBody UserUpdateRequest updateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findByUsername(username);
        if (!userOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        User existingUser = userOptional.get();
        
        // Обновляем поля если они переданы
        if (updateRequest.getDisplayName() != null) {
            existingUser.setDisplayName(updateRequest.getDisplayName());
        }
        if (updateRequest.getBio() != null) {
            existingUser.setBio(updateRequest.getBio());
        }
        if (updateRequest.getAvatarUrl() != null) {
            // Если передан новый URL аватарки
            String newAvatarUrl = updateRequest.getAvatarUrl();
            
            // Если пустая строка - удаляем аватарку
            if (newAvatarUrl.isEmpty()) {
                deleteOldAvatar(existingUser.getAvatarUrl());
                existingUser.setAvatarUrl(null);
            } 
            // Иначе обновляем URL
            else {
                // Удаляем старую аватарку если она была
                deleteOldAvatar(existingUser.getAvatarUrl());
                existingUser.setAvatarUrl(newAvatarUrl);
            }
        }
        if (updateRequest.getIsPublic() != null) {
            existingUser.setIsPublic(updateRequest.getIsPublic());
        }
        
        User updatedUser = userService.save(existingUser);
        return ResponseEntity.ok(new UserDto(updatedUser));
    }
    
    // Отдельный метод только для загрузки аватарки (опционально можно оставить)
    @PostMapping("/me/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findByUsername(username);
        if (!userOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        User existingUser = userOptional.get();
        
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            
            // Проверяем тип файла
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(null);
            }
            
            // Сохраняем файл
            String newAvatarUrl = fileStorageService.uploadFile(file);
            System.out.println("Avatar uploaded: " + newAvatarUrl);
            
            // Удаляем старую аватарку если она была
            deleteOldAvatar(existingUser.getAvatarUrl());
            
            // Обновляем URL аватарки
            existingUser.setAvatarUrl(newAvatarUrl);
            
            User updatedUser = userService.save(existingUser);
            return ResponseEntity.ok(new UserDto(updatedUser));
            
        } catch (Exception e) {
            System.err.println("Error uploading avatar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    // Метод для удаления аватарки
    @DeleteMapping("/me/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> deleteAvatar() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findByUsername(username);
        if (!userOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        User existingUser = userOptional.get();
        deleteOldAvatar(existingUser.getAvatarUrl());
        existingUser.setAvatarUrl(null);
        
        User updatedUser = userService.save(existingUser);
        return ResponseEntity.ok(new UserDto(updatedUser));
    }
    
    @GetMapping
    @PreAuthorize("isAuthenticated()") 
    public List<UserDto> getAllUsers() {
        return userService.findAll().stream()
                .filter(User::getIsPublic) 
                .map(UserDto::new)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getIsPublic() || isCurrentUser(user.getUsername())) {
                return ResponseEntity.ok(new UserDto(user));
            }
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/username/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getIsPublic() || isCurrentUser(username)) {
                return ResponseEntity.ok(new UserDto(user));
            }
        }
        return ResponseEntity.notFound().build();
    }
    
    // Вспомогательный метод для удаления старой аватарки
    private void deleteOldAvatar(String oldAvatarUrl) {
        if (oldAvatarUrl != null && oldAvatarUrl.startsWith("/uploads/images/")) {
            try {
                fileStorageService.deleteFile(oldAvatarUrl);
                System.out.println("Old avatar deleted: " + oldAvatarUrl);
            } catch (Exception e) {
                System.err.println("Error deleting old avatar: " + e.getMessage());
                // Не прерываем выполнение если не удалось удалить старый файл
            }
        }
    }
    
    private boolean isCurrentUser(String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && username.equals(auth.getName());
    }
}