package com.example.artship.social.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.artship.social.dto.RoleStatistics;
import com.example.artship.social.dto.UserDto;
import com.example.artship.social.response.UserUpdateResponse;
import com.example.artship.social.model.User;
import com.example.artship.social.model.UserRole;
import com.example.artship.social.security.JwtTokenUtil;
import com.example.artship.social.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;

import com.example.artship.social.service.LocalFileStorageService;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "Endpoints for managing user profiles")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private LocalFileStorageService fileStorageService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @GetMapping("/public/{id}")
    @Operation(summary = "Get public user by ID", description = "Returns user profile if it's public")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found or not public")
    })
    public ResponseEntity<UserDto> getPublicUser(@Parameter(description = "User ID") @PathVariable Long id) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isPresent() && userOptional.get().getIsPublic()) {
            return ResponseEntity.ok(new UserDto(userOptional.get()));
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user profile", description = "Returns profile of authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isPresent()) {
            return ResponseEntity.ok(new UserDto(userOptional.get()));
        }
        return ResponseEntity.notFound().build();
    }
    
    @PutMapping(value = "/me", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update current user profile",
        description = """
            Updates the authenticated user's profile information.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                     content = @Content(schema = @Schema(implementation = UserUpdateResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data or file type"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserUpdateResponse> updateCurrentUser(
            @Parameter(description = "New username (optional)")
            @RequestParam(value = "username", required = false) 
            @Size(max = 50, message = "Username cannot exceed 50 characters")
            String username,
            
            @Parameter(description = "Display name (optional)")
            @RequestParam(value = "displayName", required = false) 
            @Size(max = 100, message = "Display name cannot exceed 100 characters")
            String displayName,
            
            @Parameter(description = "User bio (optional)")
            @RequestParam(value = "bio", required = false) 
            @Size(max = 500, message = "Bio cannot exceed 500 characters")
            String bio,
            
            @Parameter(description = "Profile visibility (optional)")
            @RequestParam(value = "isPublic", required = false) Boolean isPublic,
            
            @Parameter(description = "Avatar URL (optional - use this OR avatarFile)")
            @RequestParam(value = "avatarUrl", required = false) String avatarUrl,
            
            @Parameter(description = "Avatar image file (JPEG, PNG, GIF). Optional.", 
                      content = @Content(mediaType = "multipart/form-data",
                                       schema = @Schema(type = "string", format = "binary")))
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        Optional<User> userOptional = userService.findByUsername(currentUsername);
        if (!userOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        User existingUser = userOptional.get();
        boolean usernameChanged = false;
        String oldUsername = existingUser.getUsername();
        
        System.out.println("=== Updating user profile ===");
        System.out.println("Current username: " + currentUsername);
        
        if (username != null && !username.equals(existingUser.getUsername())) {
            Optional<User> userWithNewUsername = userService.findByUsername(username);
            if (userWithNewUsername.isPresent()) {
                System.err.println("Username already taken: " + username);
                return ResponseEntity.badRequest().build();
            }
            
            existingUser.setUsername(username);
            usernameChanged = true;
            System.out.println("Username changed from '" + oldUsername + "' to '" + username + "'");
        }
        
        // 2. Обновляем displayName
        if (displayName != null) {
            existingUser.setDisplayName(displayName);
            System.out.println("DisplayName updated to: " + displayName);
        }
        
        if (bio != null) {
            existingUser.setBio(bio);
            System.out.println("Bio updated");
        }
        
        if (isPublic != null) {
            existingUser.setIsPublic(isPublic);
            System.out.println("IsPublic updated to: " + isPublic);
        }
        
        try {
            handleAvatarUpdate(existingUser, avatarFile, avatarUrl);
        } catch (Exception e) {
            System.err.println("Error handling avatar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        User updatedUser = userService.save(existingUser);
        System.out.println("User saved successfully");
        
        UserDto userDto = new UserDto(updatedUser);
        
        if (usernameChanged) {
            String newToken = jwtTokenUtil.generateAccessToken(updatedUser);
            System.out.println("New JWT token generated for user: " + updatedUser.getUsername());
            
            updateSecurityContext(newToken);
            
            return ResponseEntity.ok(new UserUpdateResponse(userDto, newToken));
        }
        
        return ResponseEntity.ok(new UserUpdateResponse(userDto));
    }
    

    private void handleAvatarUpdate(User user, MultipartFile avatarFile, String avatarUrl) throws Exception {
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Загружаем новый файл аватара
            String contentType = avatarFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("File must be an image");
            }
            
            String newAvatarUrl = fileStorageService.uploadFile(avatarFile);
            System.out.println("Avatar file uploaded: " + newAvatarUrl);
            
            deleteOldAvatar(user.getAvatarUrl());
            user.setAvatarUrl(newAvatarUrl);
            
        } else if (avatarUrl != null) {
            if (avatarUrl.isEmpty()) {

                deleteOldAvatar(user.getAvatarUrl());
                user.setAvatarUrl(null);
                System.out.println("Avatar removed");
            } else {
                deleteOldAvatar(user.getAvatarUrl());
                user.setAvatarUrl(avatarUrl);
                System.out.println("Avatar URL updated to: " + avatarUrl);
            }
        }
    }

    private void updateSecurityContext(String newToken) {
        // Получаем username из нового токена
        String newUsername = jwtTokenUtil.getUsernameFromToken(newToken);
        
        // Создаем новую аутентификацию
        org.springframework.security.core.Authentication newAuth = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                newUsername,
                null,
                new java.util.ArrayList<>()
            );
        

        SecurityContextHolder.getContext().setAuthentication(newAuth);
        System.out.println("SecurityContext updated with new username: " + newUsername);
    }

    @DeleteMapping("/me/avatar")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete current user's avatar", description = "Removes avatar from authenticated user's profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Avatar deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
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
    @Operation(summary = "Get all public users", description = "Returns list of all public user profiles")
    @ApiResponse(responseCode = "200", description = "List of public users")
    public List<UserDto> getAllUsers() {
        return userService.findAll().stream()
                .filter(User::getIsPublic) 
                .map(UserDto::new)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user by ID", description = "Returns user profile if public or current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "User not found or access denied")
    })
    public ResponseEntity<UserDto> getUserById(@Parameter(description = "User ID") @PathVariable Long id) {
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
    @Operation(summary = "Get user by username", description = "Returns user profile if public or current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "User not found or access denied")
    })
    public ResponseEntity<UserDto> getUserByUsername(@Parameter(description = "Username") @PathVariable String username) {
        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getIsPublic() || isCurrentUser(username)) {
                return ResponseEntity.ok(new UserDto(user));
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{userId}/role")
    @Operation(summary = "Изменить роль пользователя по ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> changeUserRoleById(
            @Parameter(description = "ID пользователя") @PathVariable Long userId,
            @Parameter(description = "Новая роль") @RequestParam UserRole role) {
        
        User updatedUser = userService.changeUserRole(userId, role);
        return ResponseEntity.ok(new UserDto(updatedUser));
    }


    @PutMapping("/username/{username}/role")
    @Operation(summary = "Изменить роль пользователя по username")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> changeUserRoleByUsername(
            @Parameter(description = "Имя пользователя") @PathVariable String username,
            @Parameter(description = "Новая роль") @RequestParam UserRole role) {
        
        User updatedUser = userService.changeUserRoleByUsername(username, role);
        return ResponseEntity.ok(new UserDto(updatedUser));
    }


    @GetMapping("/role/{role}")
    @Operation(summary = "Получить пользователей по роли")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Page<UserDto>> getUsersByRole(
            @Parameter(description = "Роль") @PathVariable UserRole role,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = userService.getUsersByRole(role, pageable);
        Page<UserDto> userDtos = users.map(UserDto::new);
        
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/role/statistics")
    @Operation(summary = "Получить статистику по ролям")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleStatistics> getRoleStatistics() {
        RoleStatistics statistics = userService.getRoleStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/admins")
    @Operation(summary = "Получить всех администраторов")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<List<UserDto>> getAdmins() {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<User> adminsPage = userService.getUsersByRole(UserRole.ADMIN, pageable);
        
        List<UserDto> admins = adminsPage.getContent()
                .stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(admins);
    }

    @PostMapping("/role/bulk")
    @Operation(summary = "Массовое изменение ролей")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> bulkChangeRole(
            @RequestBody List<Long> userIds,
            @RequestParam UserRole role) {
        
        int updatedCount = userService.bulkChangeUserRole(userIds, role);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Roles updated successfully");
        response.put("updatedCount", updatedCount);
        response.put("role", role);
        
        return ResponseEntity.ok(response);
    }

    private void deleteOldAvatar(String oldAvatarUrl) {
        if (oldAvatarUrl != null && oldAvatarUrl.startsWith("/uploads/images/")) {
            try {
                fileStorageService.deleteFile(oldAvatarUrl);
                System.out.println("Old avatar deleted: " + oldAvatarUrl);
            } catch (Exception e) {
                System.err.println("Error deleting old avatar: " + e.getMessage());
            }
        }
    }
    
    private boolean isCurrentUser(String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && username.equals(auth.getName());
    }
}