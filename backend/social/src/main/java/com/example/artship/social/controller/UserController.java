package com.example.artship.social.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.artship.social.dto.UserDto;
import com.example.artship.social.requests.UserUpdateRequest;
import com.example.artship.social.model.User;
import com.example.artship.social.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.example.artship.social.service.LocalFileStorageService;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "Endpoints for managing user profiles")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private LocalFileStorageService fileStorageService;
    
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
            - Each field is sent as separate form-data parameter
            - Avatar file upload is optional
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data or file type"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserDto> updateCurrentUser(
            @Parameter(description = "New username (optional)")
            @RequestParam(value = "username", required = false) String username,
            
            @Parameter(description = "Display name (optional)")
            @RequestParam(value = "displayName", required = false) String displayName,
            
            @Parameter(description = "User bio (optional)")
            @RequestParam(value = "bio", required = false) String bio,
            
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
        
        try {
            if (avatarFile != null && !avatarFile.isEmpty()) {
                String contentType = avatarFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    System.err.println("Invalid file type: " + contentType);
                    return ResponseEntity.badRequest().build();
                }
                
                String newAvatarUrl = fileStorageService.uploadFile(avatarFile);
                System.out.println("Avatar uploaded: " + newAvatarUrl);
                
                deleteOldAvatar(existingUser.getAvatarUrl());
                existingUser.setAvatarUrl(newAvatarUrl);
                
            } else if (avatarUrl != null) {
                if (avatarUrl.isEmpty()) {
                    deleteOldAvatar(existingUser.getAvatarUrl());
                    existingUser.setAvatarUrl(null);
                    System.out.println("Avatar removed");
                } else {
                    deleteOldAvatar(existingUser.getAvatarUrl());
                    existingUser.setAvatarUrl(avatarUrl);
                    System.out.println("Avatar URL updated to: " + avatarUrl);
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling avatar: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        if (username != null && !username.equals(existingUser.getUsername())) {
            Optional<User> userWithNewUsername = userService.findByUsername(username);
            if (userWithNewUsername.isPresent()) {
                System.err.println("Username already taken: " + username);
                return ResponseEntity.badRequest().build();
            }
            existingUser.setUsername(username);
            System.out.println("Username updated to: " + username);
        }
        
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
        
        User updatedUser = userService.save(existingUser);
        System.out.println("User updated successfully: " + updatedUser.getUsername());
        
        return ResponseEntity.ok(new UserDto(updatedUser));
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