package com.example.artship.social.service;

import com.example.artship.social.auth.AuthRequest;
import com.example.artship.social.auth.AuthResponse;
import com.example.artship.social.auth.RefreshTokenRequest;
import com.example.artship.social.dto.UserDto;
import com.example.artship.social.model.RefreshToken;
import com.example.artship.social.model.User;
import com.example.artship.social.repository.UserRepository;
import com.example.artship.social.security.CustomUserDetails;
import com.example.artship.social.security.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@Transactional
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private RefreshTokenService refreshTokenService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public AuthResponse authenticate(AuthRequest authRequest, HttpServletRequest request) {
        logger.info("=== НАЧАЛО АУТЕНТИФИКАЦИИ ===");
        logger.info("Пользователь: {}", authRequest.getUsername());
        
        try {
            // Логируем информацию о пароле
            String password = authRequest.getPassword();
            logger.debug("Длина пароля: {} символов, {} байт (UTF-8)", 
                password.length(), password.getBytes(StandardCharsets.UTF_8).length);
            
            // Проверяем, не слишком ли длинный пароль для BCrypt
            if (password.length() > 72) {
                logger.warn("Пароль длиннее 72 символов, будет обрезан BCrypt");
                // Не обрезаем здесь - BCrypt сделает это сам
            }
            
            logger.debug("Создание UsernamePasswordAuthenticationToken...");
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), password);
            
            logger.info("Вызов authenticationManager.authenticate()...");
            Authentication authentication;
            try {
                authentication = authenticationManager.authenticate(authToken);
                logger.info("✅ Аутентификация УСПЕШНА для пользователя: {}", authRequest.getUsername());
            } catch (AuthenticationException e) {
                logger.error("❌ Ошибка аутентификации для пользователя {}: {}", 
                    authRequest.getUsername(), e.getMessage());
                throw e;
            }
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Получаем детали пользователя
            logger.debug("Получение CustomUserDetails из authentication...");
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            logger.debug("ID пользователя: {}", userDetails.getId());
            
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> {
                        logger.error("❌ Пользователь с ID {} не найден в БД", userDetails.getId());
                        return new RuntimeException("User not found");
                    });
            
            logger.debug("Пользователь найден в БД: {} (ID: {})", user.getUsername(), user.getId());
            
            // Генерация access token
            logger.info("Генерация access token...");
            String accessToken = jwtTokenUtil.generateAccessToken(userDetails);
            logger.debug("Access token сгенерирован, длина: {} символов", accessToken.length());
            
            // Генерация refresh token
            logger.info("Генерация refresh token...");
            String deviceInfo = extractDeviceInfo(request);
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            
            logger.debug("Информация об устройстве: {}", deviceInfo);
            logger.debug("IP адрес: {}", ipAddress);
            logger.debug("User-Agent: {}", userAgent);
            
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                user,
                deviceInfo,
                ipAddress,
                userAgent
            );
            
            logger.debug("Refresh token создан, ID: {}", refreshToken.getId());
            
            // Получаем время истечения токена
            long expiresIn = jwtTokenUtil.getExpirationDateFromToken(accessToken).getTime();
            logger.debug("Access token истекает через: {} мс ({} минут)", 
                expiresIn, expiresIn / 60000);
            
            // Создаем ответ
            AuthResponse response = new AuthResponse(
                accessToken,
                refreshToken.getTokenHash(), // raw token (не хеш!)
                expiresIn,
                new UserDto(user)
            );
            
            logger.info("=== ✅ АУТЕНТИФИКАЦИЯ ЗАВЕРШЕНА УСПЕШНО ===");
            logger.info("Пользователь {} успешно авторизован", authRequest.getUsername());
            
            return response;
            
        } catch (Exception e) {
            logger.error("=== ❌ КРИТИЧЕСКАЯ ОШИБКА АУТЕНТИФИКАЦИИ ===", e);
            logger.error("Тип исключения: {}", e.getClass().getName());
            logger.error("Сообщение: {}", e.getMessage());
            
            // Логируем стектрейс для отладки
            if (e.getCause() != null) {
                logger.error("Причина: {}", e.getCause().getMessage());
                logger.error("Класс причины: {}", e.getCause().getClass().getName());
            }
            
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }
    
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest, HttpServletRequest request) {
        logger.info("=== ОБНОВЛЕНИЕ ТОКЕНА ===");
        
        String rawRefreshToken = refreshTokenRequest.getRefreshToken();
        if (rawRefreshToken == null || rawRefreshToken.trim().isEmpty()) {
            logger.error("Refresh token не предоставлен");
            throw new RuntimeException("Refresh token is required");
        }
        
        logger.debug("Refresh token получен, длина: {} символов", rawRefreshToken.length());
        
        // Поиск refresh token в БД
        RefreshToken refreshToken = refreshTokenService.findByToken(rawRefreshToken)
                .orElseThrow(() -> {
                    logger.error("Refresh token не найден или невалиден");
                    return new RuntimeException("Invalid refresh token");
                });
        
        logger.debug("Refresh token найден в БД, ID: {}, user ID: {}", 
            refreshToken.getId(), refreshToken.getUser().getId());
        
        // Проверка валидности токена
        if (!refreshToken.isValid()) {
            logger.error("Refresh token истек или отозван. Expiry: {}, Revoked: {}", 
                refreshToken.getExpiryDate(), refreshToken.isRevoked());
            throw new RuntimeException("Refresh token is expired or revoked");
        }
        
        // Отзыв старого refresh token
        logger.debug("Отзыв старого refresh token...");
        refreshTokenService.revokeToken(rawRefreshToken);
        
        User user = refreshToken.getUser();
        logger.debug("Создание CustomUserDetails для пользователя: {}", user.getUsername());
        CustomUserDetails userDetails = new CustomUserDetails(user);
        
        // Генерация нового access token
        logger.info("Генерация нового access token...");
        String newAccessToken = jwtTokenUtil.generateAccessToken(userDetails);
        logger.debug("Новый access token сгенерирован, длина: {} символов", newAccessToken.length());
        
        // Генерация нового refresh token
        logger.info("Генерация нового refresh token...");
        String deviceInfo = extractDeviceInfo(request);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(
            user,
            deviceInfo,
            request.getRemoteAddr(),
            request.getHeader("User-Agent")
        );
        
        logger.debug("Новый refresh token создан, ID: {}", newRefreshToken.getId());
        
        // Получаем время истечения нового access token
        long expiresIn = jwtTokenUtil.getExpirationDateFromToken(newAccessToken).getTime();
        
        logger.info("✅ Токены успешно обновлены для пользователя: {}", user.getUsername());
        
        return new AuthResponse(
            newAccessToken,
            newRefreshToken.getTokenHash(), // raw token
            expiresIn,
            new UserDto(user)
        );
    }
    
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            logger.warn("Попытка выхода без refresh token");
            return;
        }
        
        logger.info("Выход пользователя, refresh token длина: {}", refreshToken.length());
        try {
            refreshTokenService.revokeToken(refreshToken);
            logger.info("✅ Пользователь успешно вышел");
        } catch (Exception e) {
            logger.error("Ошибка при выходе пользователя: {}", e.getMessage());
            throw e;
        }
    }
    
    @Transactional
    public void logoutAll(Long userId) {
        logger.info("Выход со всех устройств для пользователя ID: {}", userId);
        try {
            refreshTokenService.revokeAllUserTokens(userId);
            logger.info("✅ Все сессии пользователя ID: {} завершены", userId);
        } catch (Exception e) {
            logger.error("Ошибка при выходе со всех устройств: {}", e.getMessage());
            throw e;
        }
    }
    
    @Transactional
    public User register(AuthRequest authRequest) {
        logger.info("=== РЕГИСТРАЦИЯ НОВОГО ПОЛЬЗОВАТЕЛЯ ===");
        logger.info("Имя пользователя: {}", authRequest.getUsername());
        
        if (authRequest.getUsername() == null || authRequest.getUsername().trim().isEmpty()) {
            logger.error("Имя пользователя не может быть пустым");
            throw new RuntimeException("Username cannot be empty");
        }
        
        if (authRequest.getPassword() == null || authRequest.getPassword().trim().isEmpty()) {
            logger.error("Пароль не может быть пустым");
            throw new RuntimeException("Password cannot be empty");
        }
        
        String username = authRequest.getUsername().trim();
        String password = authRequest.getPassword();
        
        // Проверка существования пользователя
        if (userRepository.existsByUsername(username)) {
            logger.error("❌ Имя пользователя '{}' уже существует", username);
            throw new RuntimeException("Username already exists");
        }
        
        // Для простоты используем username как email
        if (userRepository.existsByEmail(username)) {
            logger.error("❌ Email '{}' уже существует", username);
            throw new RuntimeException("Email already exists");
        }
        

        logger.debug("Пароль при регистрации, длина: {} символов, {} байт (UTF-8)", 
            password.length(), password.getBytes(StandardCharsets.UTF_8).length);
        

        if (password.length() > 72) {
            logger.warn("⚠️ Пароль длиннее 72 символов, будет обрезан BCrypt");
        }

        logger.debug("Создание объекта User...");
        User user = new User();
        user.setUsername(username);
        user.setEmail(username); 
        user.setDisplayName(username);
        
  
        logger.debug("Хеширование пароля с помощью BCrypt...");
        String hashedPassword = passwordEncoder.encode(password);
        logger.debug("Хешированный пароль, длина: {} символов", hashedPassword.length());
        logger.debug("Начало хеша: {}", 
            hashedPassword.length() > 20 ? hashedPassword.substring(0, 20) + "..." : hashedPassword);
        
 
        if (!hashedPassword.startsWith("$2")) {
            logger.warn("⚠️ Хеш не имеет формат BCrypt! Проверьте PasswordEncoder");
        }
        
        user.setPasswordHash(hashedPassword);
        

        logger.debug("Сохранение пользователя в БД...");
        User savedUser = userRepository.save(user);
        
        logger.info("=== ✅ РЕГИСТРАЦИЯ УСПЕШНА ===");
        logger.info("Пользователь '{}' зарегистрирован с ID: {}", 
            savedUser.getUsername(), savedUser.getId());
        
        return savedUser;
    }
    
    // Метод для регистрации с отдельным email
    @Transactional
    public User registerWithEmail(AuthRequest authRequest, String email) {
        logger.info("=== РЕГИСТРАЦИЯ С ОТДЕЛЬНЫМ EMAIL ===");
        logger.info("Username: {}, Email: {}", authRequest.getUsername(), email);
        
        // Проверка существования пользователя
        if (userRepository.existsByUsername(authRequest.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        
        String password = authRequest.getPassword();
        if (password.length() > 72) {
            logger.warn("Пароль будет обрезан BCrypt");
        }
        
        User user = new User(
            authRequest.getUsername(),
            email,
            passwordEncoder.encode(password),
            authRequest.getUsername()
        );
        
        User savedUser = userRepository.save(user);
        logger.info("Пользователь зарегистрирован: {} (ID: {})", 
            savedUser.getUsername(), savedUser.getId());
        
        return savedUser;
    }
    
    // Метод для проверки пользователя по username или email
    public Optional<User> findByUsernameOrEmail(String identifier) {
        logger.debug("Поиск пользователя по identifier: {}", identifier);
        return userRepository.findByUsername(identifier)
                .or(() -> {
                    logger.debug("Пользователь не найден по username, ищем по email...");
                    return userRepository.findByEmail(identifier);
                });
    }
    
    // Метод для изменения пароля
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        logger.info("Смена пароля для пользователя: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Пользователь '{}' не найден", username);
                    return new RuntimeException("User not found");
                });
        
        logger.debug("Проверка старого пароля...");
        // Проверка старого пароля
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            logger.error("Старый пароль неверный для пользователя: {}", username);
            throw new RuntimeException("Invalid old password");
        }
        
        logger.debug("Старый пароль верный. Хеширование нового пароля...");
        // Установка нового пароля
        if (newPassword.length() > 72) {
            logger.warn("Новый пароль длиннее 72 символов, будет обрезан");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        
        userRepository.save(user);
        logger.info("✅ Пароль успешно изменен для пользователя: {}", username);
    }
    
    // Метод для проверки пароля (для отладки)
    public boolean checkPassword(String username, String password) {
        logger.debug("Проверка пароля для пользователя: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Пользователь '{}' не найден при проверке пароля", username);
                    return new RuntimeException("User not found");
                });
        
        String storedHash = user.getPasswordHash();
        logger.debug("Хеш из БД, длина: {} символов", storedHash.length());
        logger.debug("Начало хеша из БД: {}", 
            storedHash.length() > 20 ? storedHash.substring(0, 20) + "..." : storedHash);
        
        boolean matches = passwordEncoder.matches(password, storedHash);
        logger.debug("Результат проверки пароля: {}", matches);
        
        return matches;
    }
    
    // Метод для получения информации о пользователе (для отладки)
    public void logUserPasswordInfo(String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            logger.info("=== ИНФОРМАЦИЯ О ПОЛЬЗОВАТЕЛЕ ===");
            logger.info("Username: {}", user.getUsername());
            logger.info("Email: {}", user.getEmail());
            logger.info("Password hash length: {}", 
                user.getPasswordHash() != null ? user.getPasswordHash().length() : 0);
            
            if (user.getPasswordHash() != null) {
                logger.info("Password hash prefix: {}", 
                    user.getPasswordHash().substring(0, Math.min(30, user.getPasswordHash().length())));
                logger.info("BCrypt prefix: {}", 
                    user.getPasswordHash().substring(0, Math.min(7, user.getPasswordHash().length())));
                
                // Проверяем формат BCrypt
                if (user.getPasswordHash().startsWith("$2")) {
                    logger.info("✅ Хеш имеет формат BCrypt");
                } else {
                    logger.warn("⚠️ Хеш НЕ имеет формат BCrypt!");
                }
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении информации о пользователе: {}", e.getMessage());
        }
    }
    
    // Метод для проверки возможности аутентификации (для тестов)
    public boolean canAuthenticate(String username, String password) {
        try {
            logger.debug("Проверка возможности аутентификации для пользователя: {}", username);
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            boolean isAuthenticated = authentication.isAuthenticated();
            logger.debug("Результат проверки аутентификации: {}", isAuthenticated);
            return isAuthenticated;
        } catch (AuthenticationException e) {
            logger.debug("Аутентификация невозможна: {}", e.getMessage());
            return false;
        }
    }
    
    // Извлечение информации об устройстве
    private String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String deviceInfo = userAgent != null ? 
            userAgent.substring(0, Math.min(userAgent.length(), 500)) : "Unknown";
        
        logger.debug("Извлечена информация об устройстве: {}...", 
            deviceInfo.length() > 50 ? deviceInfo.substring(0, 50) + "..." : deviceInfo);
        
        return deviceInfo;
    }
    
    // Метод для получения текущего пользователя из SecurityContext
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            logger.debug("Текущий аутентифицированный пользователь: {}", username);
            return userRepository.findByUsername(username);
        }
        logger.debug("Нет аутентифицированного пользователя");
        return Optional.empty();
    }
    
    // Метод для принудительной аутентификации (для тестов)
    @Transactional
    public void forceAuthenticate(String username, String password) {
        logger.info("Принудительная аутентификация пользователя: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Проверяем пароль
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }
        
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.info("✅ Пользователь {} принудительно аутентифицирован", username);
    }
}