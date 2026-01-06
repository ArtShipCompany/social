package com.example.artship.social.controller;



import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.artship.social.dto.UserCreateRequest;
import com.example.artship.social.dto.UserDto;
import com.example.artship.social.dto.UserUpdateRequest;
import com.example.artship.social.model.User;
import com.example.artship.social.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder; 
    
    @GetMapping("/public/{id}")
    public ResponseEntity<UserDto> getPublicUser(@PathVariable Long id) {
        return userService.findById(id)
                .filter(User::getIsPublic) 
                .map(UserDto::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        return userService.findByUsername(username)
                .map(UserDto::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> updateCurrentUser(@RequestBody UserUpdateRequest updateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        return userService.findByUsername(username)
                .map(existingUser -> {
                    if (updateRequest.getDisplayName() != null) {
                        existingUser.setDisplayName(updateRequest.getDisplayName());
                    }
                    if (updateRequest.getBio() != null) {
                        existingUser.setBio(updateRequest.getBio());
                    }
                    if (updateRequest.getAvatarUrl() != null) {
                        existingUser.setAvatarUrl(updateRequest.getAvatarUrl());
                    }
                    if (updateRequest.getIsPublic() != null) {
                        existingUser.setIsPublic(updateRequest.getIsPublic());
                    }
                    
                    User updatedUser = userService.save(existingUser);
                    return ResponseEntity.ok(new UserDto(updatedUser));
                })
                .orElse(ResponseEntity.notFound().build());
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
        return userService.findById(id)
                .filter(user -> user.getIsPublic() || 
                        isCurrentUser(user.getUsername())) 
                .map(UserDto::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    

    
    
    @GetMapping("/username/{username}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                .filter(user -> user.getIsPublic() || isCurrentUser(username))
                .map(UserDto::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    private boolean isCurrentUser(String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && username.equals(auth.getName());
    }
}