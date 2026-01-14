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
import java.util.Date;
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
        logger.info("Request time: {}", new Date());
        
        try {
            // Логируем информацию о пароле
            String password = authRequest.getPassword();
            
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
            
            // Получаем информацию о токене для дебага
            Date issuedAt = jwtTokenUtil.getIssuedAtDateFromToken(accessToken);
            Date expiration = jwtTokenUtil.getExpirationDateFromToken(accessToken);
            String jti = jwtTokenUtil.getJtiFromToken(accessToken);
            
            
            // Вычисляем expiresIn (оставшееся время в миллисекундах)
            long currentTime = System.currentTimeMillis();
            long expiresIn = expiration.getTime() - currentTime;
            
            logger.info("Current time: {}", new Date(currentTime));
            logger.info("Expires in (remaining): {} ms ({} minutes)", 
                expiresIn, expiresIn / 60000);
            
            // Генерация refresh token
            logger.info("Генерация refresh token...");
            String deviceInfo = extractDeviceInfo(request);
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            
            logger.debug("Информация об устройстве: {}", deviceInfo);
            logger.debug("IP адрес: {}", ipAddress);
            
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                user,
                deviceInfo,
                ipAddress,
                userAgent
            );
            
            logger.debug("Refresh token создан, ID: {}", refreshToken.getId());
            logger.debug("Refresh token hash: {}", refreshToken.getTokenHash().substring(0, 20) + "...");
            
            // Создаем ответ с ПРАВИЛЬНЫМ expiresIn
            AuthResponse response = new AuthResponse(
                accessToken,
                refreshToken.getTokenHash(),
                expiresIn,  // ← ОСТАВШЕЕСЯ время жизни в миллисекундах!
                new UserDto(user)
            );
            
            logger.info("=== ✅ АУТЕНТИФИКАЦИЯ ЗАВЕРШЕНА УСПЕШНО ===");
            logger.info("Пользователь {} успешно авторизован", authRequest.getUsername());
            logger.info("Response time: {}", new Date());
            
            return response;
            
        } catch (Exception e) {
            logger.error("=== ❌ КРИТИЧЕСКАЯ ОШИБКА АУТЕНТИФИКАЦИИ ===", e);
            logger.error("Тип исключения: {}", e.getClass().getName());
            logger.error("Сообщение: {}", e.getMessage());
            
            if (e.getCause() != null) {
                logger.error("Причина: {}", e.getCause().getMessage());
                logger.error("Класс причины: {}", e.getCause().getClass().getName());
            }
            
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }
    
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest, HttpServletRequest request) {
        logger.info("=== ОБНОВЛЕНИЕ ТОКЕНА ===");
        logger.info("Request time: {}", new Date());
        
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
        
        // Получаем информацию о новом токене
        Date issuedAt = jwtTokenUtil.getIssuedAtDateFromToken(newAccessToken);
        Date expiration = jwtTokenUtil.getExpirationDateFromToken(newAccessToken);
        String jti = jwtTokenUtil.getJtiFromToken(newAccessToken);
        
        logger.info("=== NEW ACCESS TOKEN INFO ===");
        logger.info("New token JTI: {}", jti);
        logger.info("New token issued at: {}", issuedAt);
        logger.info("New token expires at: {}", expiration);
        
        // Вычисляем expiresIn для нового токена
        long currentTime = System.currentTimeMillis();
        long expiresIn = expiration.getTime() - currentTime;
        
        logger.info("Expires in (remaining): {} ms ({} minutes)", 
            expiresIn, expiresIn / 60000);
        
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
        
        logger.info("✅ Токены успешно обновлены для пользователя: {}", user.getUsername());
        logger.info("Response time: {}", new Date());
        
        return new AuthResponse(
            newAccessToken,
            newRefreshToken.getTokenHash(),
            expiresIn,  // ← ОСТАВШЕЕСЯ время жизни!
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
        logger.info("Имя пользователя: {}, Email: {}", 
            authRequest.getUsername(), authRequest.getEmail());
        
        // Валидация входных данных
        if (authRequest.getUsername() == null || authRequest.getUsername().trim().isEmpty()) {
            logger.error("Имя пользователя не может быть пустым");
            throw new RuntimeException("Username cannot be empty");
        }
        
        if (authRequest.getEmail() == null || authRequest.getEmail().trim().isEmpty()) {
            logger.error("Email не может быть пустым");
            throw new RuntimeException("Email cannot be empty");
        }
        
        if (authRequest.getPassword() == null || authRequest.getPassword().trim().isEmpty()) {
            logger.error("Пароль не может быть пустым");
            throw new RuntimeException("Password cannot be empty");
        }
        
        String username = authRequest.getUsername().trim();
        String email = authRequest.getEmail().trim();
        String password = authRequest.getPassword();
        
        // Проверка существования пользователя по username
        if (userRepository.existsByUsername(username)) {
            logger.error("❌ Имя пользователя '{}' уже существует", username);
            throw new RuntimeException("Username already exists");
        }
        
        // Проверка существования пользователя по email
        if (userRepository.existsByEmail(email)) {
            logger.error("❌ Email '{}' уже существует", email);
            throw new RuntimeException("Email already exists");
        }
        
        // Логируем информацию о пароле
        logger.debug("Пароль при регистрации, длина: {} символов, {} байт (UTF-8)", 
            password.length(), password.getBytes(StandardCharsets.UTF_8).length);
        
        // BCrypt автоматически обрежет пароль если он длиннее 72 символов
        if (password.length() > 72) {
            logger.warn("⚠️ Пароль длиннее 72 символов, будет обрезан BCrypt");
        }
        
        // Создание нового пользователя с ОТДЕЛЬНЫМ email
        logger.debug("Создание объекта User...");
        User user = new User();
        user.setUsername(username);
        user.setEmail(email); // ← ИСПРАВЛЕНИЕ: используем email из запроса, а не username
        user.setDisplayName(username); // Можно использовать username как displayName по умолчанию
        
        // Хеширование пароля с помощью BCrypt
        logger.debug("Хеширование пароля с помощью BCrypt...");
        String hashedPassword = passwordEncoder.encode(password);
        logger.debug("Хешированный пароль, длина: {} символов", hashedPassword.length());
        logger.debug("Начало хеша: {}", 
            hashedPassword.length() > 20 ? hashedPassword.substring(0, 20) + "..." : hashedPassword);
        
        // Проверяем формат BCrypt
        if (!hashedPassword.startsWith("$2")) {
            logger.warn("⚠️ Хеш не имеет формат BCrypt! Проверьте PasswordEncoder");
        }
        
        user.setPasswordHash(hashedPassword);
        
        // Сохранение пользователя
        logger.debug("Сохранение пользователя в БД...");
        User savedUser = userRepository.save(user);
        
        logger.info("=== ✅ РЕГИСТРАЦИЯ УСПЕШНА ===");
        logger.info("Пользователь '{}' зарегистрирован с ID: {} и email: {}", 
            savedUser.getUsername(), savedUser.getId(), savedUser.getEmail());
        
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