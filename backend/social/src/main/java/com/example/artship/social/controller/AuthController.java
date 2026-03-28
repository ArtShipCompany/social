package com.example.artship.social.controller;

import com.example.artship.social.auth.AuthRequest;
import com.example.artship.social.auth.AuthResponse;
import com.example.artship.social.auth.LogoutRequest;
import com.example.artship.social.auth.RefreshTokenRequest;
import com.example.artship.social.dto.*;
import com.example.artship.social.model.User;
import com.example.artship.social.service.AuthService;
import com.example.artship.social.requests.ResetPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API для аутентификации и управления пользователями")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    @Operation(summary = "Вход в систему", description = "Аутентификация пользователя и получение токенов")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешный вход"),
        @ApiResponse(responseCode = "401", description = "Неверные учетные данные или email не подтвержден")
    })
    public ResponseEntity<?> login(
            @Valid @RequestBody AuthRequest authRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        try {
            AuthResponse authResponse = authService.authenticate(authRequest, request, response);
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
    
    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя", description = "Создает нового пользователя и отправляет письмо для подтверждения email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Регистрация успешна, письмо отправлено"),
        @ApiResponse(responseCode = "400", description = "Ошибка валидации или пользователь уже существует")
    })
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest authRequest) {
        try {
            User user = authService.register(authRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful. Please check your email to verify your account.");
            response.put("user", new UserDto(user));
            response.put("requiresEmailVerification", true);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/verify")
    @Operation(summary = "Подтверждение email", description = "Подтверждает email пользователя по токену")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email успешно подтвержден"),
        @ApiResponse(responseCode = "400", description = "Неверный или просроченный токен")
    })
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            authService.verifyEmail(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Email verified successfully");
            response.put("status", "success");
            response.put("verified", true);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("status", "failed");
            response.put("verified", false);
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/resend-verification")
    @Operation(summary = "Повторная отправка письма подтверждения", description = "Отправляет новое письмо для подтверждения email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Письмо отправлено"),
        @ApiResponse(responseCode = "400", description = "Пользователь не найден или email уже подтвержден")
    })
    public ResponseEntity<?> resendVerification(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Email пользователя", required = true)
            @RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            authService.resendVerificationEmail(email);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Verification email has been resent. Please check your inbox.");
            response.put("email", email);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/forgot-password")
    @Operation(summary = "Запрос на сброс пароля", description = "Отправляет письмо со ссылкой для сброса пароля")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Письмо отправлено"),
        @ApiResponse(responseCode = "400", description = "Пользователь не найден")
    })
    public ResponseEntity<?> forgotPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Email пользователя", required = true, content = @Content(schema = @Schema(example = "{\"email\": \"user@example.com\"}")))
            @RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            authService.requestPasswordReset(email);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset email has been sent. Please check your inbox.");
            response.put("email", email);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Сброс пароля", description = "Устанавливает новый пароль по токену из письма")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Пароль успешно изменен"),
        @ApiResponse(responseCode = "400", description = "Неверный или просроченный токен")
    })
    public ResponseEntity<?> resetPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Данные для сброса пароля", required = true, content = @Content(schema = @Schema(example = "{\"token\": \"9725af7c-c991-4a8f-9aed-0616f27e75fd\", \"newPassword\": \"newPassword123\"}")))
            @RequestBody ResetPasswordRequest request) {
        try {
            if (request.getToken() == null || request.getToken().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Token is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "New password is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (request.getNewPassword().length() < 6) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Password must be at least 6 characters long");
                return ResponseEntity.badRequest().body(error);
            }
            
            authService.resetPassword(request.getToken(), request.getNewPassword());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset successfully. You can now log in with your new password.");
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "failed");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/reset-password")
    @Operation(summary = "Проверка токена сброса пароля", description = "Проверяет валидность токена перед сбросом пароля")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Токен валиден"),
        @ApiResponse(responseCode = "400", description = "Токен недействителен или истек")
    })
    public ResponseEntity<?> validateResetToken(
            @Parameter(description = "Токен сброса пароля из письма", required = true)
            @RequestParam String token) {
        
        try {
            boolean isValid = authService.validateResetToken(token);
            
            if (!isValid) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid or expired reset token");
                error.put("status", "failed");
                return ResponseEntity.badRequest().body(error);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Token is valid");
            response.put("status", "success");
            response.put("token", token);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "failed");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Обновление токенов", description = "Получает новую пару токенов по refresh token из cookie")
    public ResponseEntity<?> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        
        try {
            AuthResponse authResponse = authService.refreshToken(request, response);
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Выход из системы", description = "Завершает сессию пользователя")
    public ResponseEntity<?> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            authService.logout(request, response);
            Map<String, String> result = new HashMap<>();
            result.put("message", "Logged out successfully");
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/logout-all")
    @Operation(summary = "Выход со всех устройств", description = "Завершает все сессии пользователя")
    public ResponseEntity<?> logoutAll(
            @RequestParam Long userId,
            HttpServletResponse response) {
        try {
            authService.logoutAll(userId);
            authService.logout(null, response);
            Map<String, String> result = new HashMap<>();
            result.put("message", "Logged out from all devices successfully");
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/verify-status")
    @Operation(summary = "Проверка статуса email", description = "Проверяет, подтвержден ли email пользователя")
    public ResponseEntity<?> checkEmailVerificationStatus(
            @Parameter(description = "Email пользователя", required = true, example = "user@example.com")
            @RequestParam String email) {
        try {
            boolean isVerified = authService.isEmailVerified(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("email", email);
            response.put("verified", isVerified);
            
            if (!isVerified) {
                response.put("message", "Email not verified. Please check your inbox for verification link.");
            }
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/requires-verification")
    @Operation(summary = "Проверка необходимости подтверждения", description = "Проверяет, нужно ли подтверждать email")
    public ResponseEntity<?> requiresEmailVerification(
            @Parameter(description = "Email пользователя", required = true, example = "user@example.com")
            @RequestParam String email) {
        try {
            boolean isVerified = authService.isEmailVerified(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("email", email);
            response.put("verified", isVerified);
            response.put("requiresVerification", !isVerified);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
