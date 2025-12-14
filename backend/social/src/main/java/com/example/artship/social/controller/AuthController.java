package com.example.artship.social.controller;

import com.example.artship.social.auth.AuthRequest;
import com.example.artship.social.auth.AuthResponse;
import com.example.artship.social.auth.LogoutRequest;
import com.example.artship.social.auth.RefreshTokenRequest;
import com.example.artship.social.dto.*;
import com.example.artship.social.model.User;
import com.example.artship.social.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest authRequest,
            HttpServletRequest request) {
        
        AuthResponse response = authService.authenticate(authRequest, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody AuthRequest authRequest) {
        User user = authService.register(authRequest);
        return ResponseEntity.ok(new UserDto(user));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest request) {
        
        AuthResponse response = authService.refreshToken(refreshTokenRequest, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody LogoutRequest logoutRequest) {
        authService.logout(logoutRequest.getRefreshToken());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(@RequestParam Long userId) {
        authService.logoutAll(userId);
        return ResponseEntity.ok().build();
    }
}