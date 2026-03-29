package com.example.artship.social.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.artship.social.auth.AuthRequest;
import com.example.artship.social.auth.AuthResponse;
import com.example.artship.social.dto.UserDto;
import com.example.artship.social.model.RefreshToken;
import com.example.artship.social.model.User;
import com.example.artship.social.model.mongo.VerificationToken;
import com.example.artship.social.repository.UserRepository;
import com.example.artship.social.repository.mongo.VerificationTokenRepository;
import com.example.artship.social.security.CustomUserDetails;
import com.example.artship.social.security.JwtTokenUtil;

@Service
@Transactional
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    
    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;
    
    @Value("${app.cookie.domain:}")
    private String cookieDomain;
    
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
    
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    
    @Autowired
    private EmailService emailService;


    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        String cookieValue = String.format(
            "refresh_token=%s; Path=/; HttpOnly; Max-Age=%d; SameSite=Lax",
            refreshToken,
            30 * 24 * 60 * 60
        );
        
        if (cookieSecure) {
            cookieValue += "; Secure";
        }
        
        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            cookieValue += "; Domain=" + cookieDomain;
        }
        
        response.addHeader("Set-Cookie", cookieValue);
        
        response.addHeader("Access-Control-Allow-Credentials", "true");
        
        logger.debug("Refresh token cookie установлен с Path=/");
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        String cookieValue = "refresh_token=; Path=/; HttpOnly; Max-Age=0; SameSite=Lax";
        
        if (cookieSecure) {
            cookieValue += "; Secure";
        }
        
        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            cookieValue += "; Domain=" + cookieDomain;
        }
        
        response.addHeader("Set-Cookie", cookieValue);
        logger.debug("Refresh token cookie удален");
    }

    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            logger.debug("Total cookies found: {}", cookies.length);
            for (Cookie cookie : cookies) {
                logger.debug("Cookie: name={}, path={}, value={}", 
                    cookie.getName(), cookie.getPath(), 
                    cookie.getValue() != null ? cookie.getValue().substring(0, Math.min(10, cookie.getValue().length())) + "..." : "null");
                
                if ("refresh_token".equals(cookie.getName())) {
                    logger.debug("Refresh token найден в cookie");
                    return cookie.getValue();
                }
            }
        } else {
            logger.warn("No cookies found in request");
        }
        
        String cookieHeader = request.getHeader("Cookie");
        if (cookieHeader != null && cookieHeader.contains("refresh_token=")) {
            logger.debug("Found refresh_token in Cookie header");
            String[] cookies_arr = cookieHeader.split(";");
            for (String c : cookies_arr) {
                c = c.trim();
                if (c.startsWith("refresh_token=")) {
                    return c.substring("refresh_token=".length());
                }
            }
        }
        
        logger.debug("Refresh token не найден");
        return null;
    }
    
    public AuthResponse authenticate(AuthRequest authRequest, 
                                      HttpServletRequest request, 
                                      HttpServletResponse response) {
        logger.info("=== НАЧАЛО АУТЕНТИФИКАЦИИ ===");
        logger.info("Пользователь: {}", authRequest.getUsername());
        logger.info("Request time: {}", new Date());
        
        try {
            String password = authRequest.getPassword();
            
            logger.debug("Создание UsernamePasswordAuthenticationToken...");
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), password);
            
            logger.info("Вызов authenticationManager.authenticate()...");
            Authentication authentication;
            try {
                authentication = authenticationManager.authenticate(authToken);
                logger.info("Аутентификация УСПЕШНА для пользователя: {}", authRequest.getUsername());
            } catch (AuthenticationException e) {
                logger.error("Ошибка аутентификации для пользователя {}: {}", 
                    authRequest.getUsername(), e.getMessage());
                throw e;
            }
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            logger.debug("Получение CustomUserDetails из authentication...");
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            logger.debug("ID пользователя: {}", userDetails.getId());
            
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> {
                        logger.error("Пользователь с ID {} не найден в БД", userDetails.getId());
                        return new RuntimeException("User not found");
                    });
            
            logger.debug("Пользователь найден в БД: {} (ID: {})", user.getUsername(), user.getId());
            
            if (!user.isEmailVerified()) {
                logger.error("Попытка входа с неподтвержденным email: {}", user.getEmail());
                throw new RuntimeException("Email not verified. Please verify your email before logging in.");
            }
            
            logger.info("Генерация access token...");
            String accessToken = jwtTokenUtil.generateAccessToken(userDetails);
            
            Date issuedAt = jwtTokenUtil.getIssuedAtDateFromToken(accessToken);
            Date expiration = jwtTokenUtil.getExpirationDateFromToken(accessToken);
            
            long currentTime = System.currentTimeMillis();
            long expiresIn = expiration.getTime() - currentTime;
            
            logger.info("Current time: {}", new Date(currentTime));
            logger.info("Expires in (remaining): {} ms ({} minutes)", 
                expiresIn, expiresIn / 60000);
            
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
            
            setRefreshTokenCookie(response, refreshToken.getTokenHash());
            
            logger.debug("Refresh token создан, ID: {}", refreshToken.getId());
            
            AuthResponse authResponse = new AuthResponse(
                accessToken,
                null, 
                expiresIn, 
                new UserDto(user)
            );
            
            logger.info("=== АУТЕНТИФИКАЦИЯ ЗАВЕРШЕНА УСПЕШНО ===");
            logger.info("Пользователь {} успешно авторизован", authRequest.getUsername());
            logger.info("Response time: {}", new Date());
            
            return authResponse;
            
        } catch (Exception e) {
            logger.error("=== КРИТИЧЕСКАЯ ОШИБКА АУТЕНТИФИКАЦИИ ===", e);
            logger.error("Тип исключения: {}", e.getClass().getName());
            logger.error("Сообщение: {}", e.getMessage());
            
            if (e.getCause() != null) {
                logger.error("Причина: {}", e.getCause().getMessage());
                logger.error("Класс причины: {}", e.getCause().getClass().getName());
            }
            
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }
    
    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        logger.info("=== ОБНОВЛЕНИЕ ТОКЕНА ===");
        logger.info("Request time: {}", new Date());
        
        String rawRefreshToken = extractRefreshTokenFromCookies(request);
        
        if (rawRefreshToken == null || rawRefreshToken.trim().isEmpty()) {
            logger.error("Refresh token не найден в cookie");
            throw new RuntimeException("Refresh token is required");
        }
        
        logger.debug("Refresh token получен из cookie, длина: {} символов", rawRefreshToken.length());
        
        RefreshToken refreshToken = refreshTokenService.findByToken(rawRefreshToken)
                .orElseThrow(() -> {
                    logger.error("Refresh token не найден или невалиден");
                    return new RuntimeException("Invalid refresh token");
                });
        
        logger.debug("Refresh token найден в БД, ID: {}, user ID: {}", 
            refreshToken.getId(), refreshToken.getUser().getId());
        
        if (!refreshToken.isValid()) {
            logger.error("Refresh token истек или отозван. Expiry: {}, Revoked: {}", 
                refreshToken.getExpiryDate(), refreshToken.isRevoked());
            throw new RuntimeException("Refresh token is expired or revoked");
        }
        
        refreshTokenService.revokeToken(rawRefreshToken);
        
        User user = refreshToken.getUser();
        
        if (!user.isEmailVerified()) {
            logger.error("Попытка обновления токена для неподтвержденного email: {}", user.getEmail());
            throw new RuntimeException("Email not verified. Please verify your email before using the service.");
        }
        
        logger.debug("Создание CustomUserDetails для пользователя: {}", user.getUsername());
        CustomUserDetails userDetails = new CustomUserDetails(user);
        
        logger.info("Генерация нового access token...");
        String newAccessToken = jwtTokenUtil.generateAccessToken(userDetails);
        
        Date expiration = jwtTokenUtil.getExpirationDateFromToken(newAccessToken);
        
        long currentTime = System.currentTimeMillis();
        long expiresIn = expiration.getTime() - currentTime;
        
        logger.info("Expires in (remaining): {} ms ({} minutes)", 
            expiresIn, expiresIn / 60000);
        
        logger.info("Генерация нового refresh token...");
        String deviceInfo = extractDeviceInfo(request);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(
            user,
            deviceInfo,
            request.getRemoteAddr(),
            request.getHeader("User-Agent")
        );
        
        setRefreshTokenCookie(response, newRefreshToken.getTokenHash());
        
        logger.debug("Новый refresh token создан, ID: {}", newRefreshToken.getId());
        
        logger.info("Токены успешно обновлены для пользователя: {}", user.getUsername());
        logger.info("Response time: {}", new Date());
        
        return new AuthResponse(
            newAccessToken,
            null, 
            expiresIn,  
            new UserDto(user)
        );
    }
    
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookies(request);
        
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            logger.warn("Попытка выхода без refresh token");
            clearRefreshTokenCookie(response);
            return;
        }
        
        logger.info("Выход пользователя, refresh token длина: {}", refreshToken.length());
        try {
            refreshTokenService.revokeToken(refreshToken);
            clearRefreshTokenCookie(response);
            logger.info("✅ Пользователь успешно вышел");
        } catch (Exception e) {
            logger.error("Ошибка при выходе пользователя: {}", e.getMessage());
            clearRefreshTokenCookie(response);
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
        
        if (userRepository.existsByUsername(username)) {
            logger.error("Имя пользователя '{}' уже существует", username);
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(email)) {
            logger.error("Email '{}' уже существует", email);
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
        user.setEmail(email); 
        user.setDisplayName(username);
        
        String hashedPassword = passwordEncoder.encode(password);
        logger.debug("Хешированный пароль, длина: {} символов", hashedPassword.length());
        
        if (!hashedPassword.startsWith("$2")) {
            logger.warn("Хеш не имеет формат BCrypt! Проверьте PasswordEncoder");
        }
        
        user.setPasswordHash(hashedPassword);
        
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        logger.debug("Сохранение пользователя в БД...");
        User savedUser = userRepository.save(user);
        
        // НОВОЕ: Создаем токен верификации в MongoDB
        String verificationToken = UUID.randomUUID().toString();
        VerificationToken token = new VerificationToken(
            verificationToken,
            savedUser.getId(),
            VerificationToken.TokenType.EMAIL_VERIFICATION,
            LocalDateTime.now().plusHours(24)
        );
        
        verificationTokenRepository.save(token);
        logger.debug("Токен верификации создан: {}", verificationToken);
        
        try {
            emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getUsername(), verificationToken);
            logger.info("Письмо для подтверждения email отправлено на: {}", savedUser.getEmail());
        } catch (Exception e) {
            logger.error("Не удалось отправить письмо подтверждения: {}", e.getMessage());
        }
        
        logger.info("Пользователь '{}' зарегистрирован с ID: {} и email: {}", 
            savedUser.getUsername(), savedUser.getId(), savedUser.getEmail());
        logger.info("⚠️ Email НЕ ПОДТВЕРЖДЕН. Пользователь должен подтвердить email перед входом.");
        
        return savedUser;
    }
    
    public void verifyEmail(String token) {
        logger.info("=== ПОДТВЕРЖДЕНИЕ EMAIL ===");
        logger.info("Token: {}", token);
        
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
            .orElseThrow(() -> {
                logger.error("Токен верификации не найден: {}", token);
                return new RuntimeException("Invalid verification token");
            });
        
        if (!verificationToken.isValid()) {
            if (verificationToken.isUsed()) {
                logger.error("Токен уже использован");
                throw new RuntimeException("Token already used");
            }
            logger.error("Токен истек: expiryDate={}", verificationToken.getExpiryDate());
            throw new RuntimeException("Verification token has expired");
        }
        
        if (verificationToken.getType() != VerificationToken.TokenType.EMAIL_VERIFICATION) {
            logger.error("Неверный тип токена: {}", verificationToken.getType());
            throw new RuntimeException("Invalid token type");
        }
        
        User user = userRepository.findById(verificationToken.getUserId())
            .orElseThrow(() -> {
                logger.error("Пользователь с ID {} не найден", verificationToken.getUserId());
                return new RuntimeException("User not found");
            });
        
        if (user.isEmailVerified()) {
            logger.warn("Email уже подтвержден для пользователя: {}", user.getEmail());
            throw new RuntimeException("Email already verified");
        }
        
        user.setEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);
        
        verificationTokenRepository.deleteByUserIdAndType(user.getId(), 
            VerificationToken.TokenType.EMAIL_VERIFICATION);
        
        logger.info("✅ Email успешно подтвержден для пользователя: {}", user.getEmail());
    }
    
    public void resendVerificationEmail(String email) {
        logger.info("=== ПОВТОРНАЯ ОТПРАВКА ПИСЬМА ПОДТВЕРЖДЕНИЯ ===");
        logger.info("Email: {}", email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                logger.error("Пользователь с email {} не найден", email);
                return new RuntimeException("User not found");
            });
        
        if (user.isEmailVerified()) {
            logger.warn("Email уже подтвержден для пользователя: {}", email);
            throw new RuntimeException("Email already verified");
        }
        
        // Ищем активные токены
        java.util.List<VerificationToken> activeTokens = verificationTokenRepository.findActiveTokensByUserAndType(
            user.getId(),
            VerificationToken.TokenType.EMAIL_VERIFICATION,
            LocalDateTime.now()
        );
        
        String tokenValue;
        
        if (!activeTokens.isEmpty()) {
            tokenValue = activeTokens.get(0).getToken();
            logger.debug("Используем существующий токен: {}", tokenValue);
        } else {
            tokenValue = UUID.randomUUID().toString();
            VerificationToken token = new VerificationToken(
                tokenValue,
                user.getId(),
                VerificationToken.TokenType.EMAIL_VERIFICATION,
                LocalDateTime.now().plusHours(24)
            );
            verificationTokenRepository.save(token);
            logger.debug("Создан новый токен: {}", tokenValue);
        }
        
        try {
            emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), tokenValue);
            logger.info("Письмо подтверждения отправлено на: {}", email);
        } catch (Exception e) {
            logger.error("Не удалось отправить письмо: {}", e.getMessage());
            throw new RuntimeException("Failed to send verification email");
        }
    }
    
    public void requestPasswordReset(String email) {
        logger.info("=== ЗАПРОС НА СБРОС ПАРОЛЯ ===");
        logger.info("Email: {}", email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                logger.error("Пользователь с email {} не найден", email);
                return new RuntimeException("User not found");
            });
        
        verificationTokenRepository.deleteByUserIdAndType(user.getId(), 
            VerificationToken.TokenType.PASSWORD_RESET);
        
        String tokenValue = UUID.randomUUID().toString();
        VerificationToken token = new VerificationToken(
            tokenValue,
            user.getId(),
            VerificationToken.TokenType.PASSWORD_RESET,
            LocalDateTime.now().plusHours(1)
        );
        
        verificationTokenRepository.save(token);
        logger.debug("Токен сброса пароля создан: {}", tokenValue);
        
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), tokenValue);
            logger.info("Письмо для сброса пароля отправлено на: {}", email);
        } catch (Exception e) {
            logger.error("Не удалось отправить письмо: {}", e.getMessage());
            throw new RuntimeException("Failed to send password reset email");
        }
    }
    
    public void resetPassword(String token, String newPassword) {
        logger.info("=== СБРОС ПАРОЛЯ ===");
        logger.info("Token: {}", token);
        
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
            .orElseThrow(() -> {
                logger.error("Токен сброса пароля не найден: {}", token);
                return new RuntimeException("Invalid reset token");
            });
        
        if (!verificationToken.isValid()) {
            if (verificationToken.isUsed()) {
                logger.error("Токен уже использован");
                throw new RuntimeException("Token already used");
            }
            logger.error("Токен истек: expiryDate={}", verificationToken.getExpiryDate());
            throw new RuntimeException("Reset token has expired");
        }
        
        if (verificationToken.getType() != VerificationToken.TokenType.PASSWORD_RESET) {
            logger.error("Неверный тип токена: {}", verificationToken.getType());
            throw new RuntimeException("Invalid token type");
        }
        
        User user = userRepository.findById(verificationToken.getUserId())
            .orElseThrow(() -> {
                logger.error("Пользователь с ID {} не найден", verificationToken.getUserId());
                return new RuntimeException("User not found");
            });
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);
        
        verificationTokenRepository.deleteByUserIdAndType(user.getId(), 
            VerificationToken.TokenType.PASSWORD_RESET);
        
        logger.info("✅ Пароль успешно сброшен для пользователя: {}", user.getEmail());
    }
    
    public boolean isEmailVerified(String email) {
        return userRepository.findByEmail(email)
            .map(User::isEmailVerified)
            .orElse(false);
    }
    
    public Optional<User> findByUsernameOrEmail(String identifier) {
        logger.debug("Поиск пользователя по identifier: {}", identifier);
        return userRepository.findByUsername(identifier)
                .or(() -> {
                    logger.debug("Пользователь не найден по username, ищем по email...");
                    return userRepository.findByEmail(identifier);
                });
    }
    
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        logger.info("Смена пароля для пользователя: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Пользователь '{}' не найден", username);
                    return new RuntimeException("User not found");
                });
        
        logger.debug("Проверка старого пароля...");
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            logger.error("Старый пароль неверный для пользователя: {}", username);
            throw new RuntimeException("Invalid old password");
        }
        
        logger.debug("Старый пароль верный. Хеширование нового пароля...");
        if (newPassword.length() > 72) {
            logger.warn("Новый пароль длиннее 72 символов, будет обрезан");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        logger.info("Пароль успешно изменен для пользователя: {}", username);
    }
    
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
    
    public void logUserPasswordInfo(String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            logger.info("=== ИНФОРМАЦИЯ О ПОЛЬЗОВАТЕЛЕ ===");
            logger.info("Username: {}", user.getUsername());
            logger.info("Email: {}", user.getEmail());
            logger.info("Email verified: {}", user.isEmailVerified());
            logger.info("Password hash length: {}", 
                user.getPasswordHash() != null ? user.getPasswordHash().length() : 0);
            
            if (user.getPasswordHash() != null) {
                logger.info("Password hash prefix: {}", 
                    user.getPasswordHash().substring(0, Math.min(30, user.getPasswordHash().length())));
                logger.info("BCrypt prefix: {}", 
                    user.getPasswordHash().substring(0, Math.min(7, user.getPasswordHash().length())));
                
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
    
    private String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String deviceInfo = userAgent != null ? 
            userAgent.substring(0, Math.min(userAgent.length(), 500)) : "Unknown";
        
        logger.debug("Извлечена информация об устройстве: {}...", 
            deviceInfo.length() > 50 ? deviceInfo.substring(0, 50) + "..." : deviceInfo);
        
        return deviceInfo;
    }
    
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

    public boolean validateResetToken(String token) {
    logger.debug("Validating reset token: {}", token);
        
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElse(null);
        
        if (verificationToken == null) {
            logger.warn("Token not found: {}", token);
            return false;
        }
        
        if (!verificationToken.isValid()) {
            logger.warn("Token is invalid or expired: {}", token);
            return false;
        }
        
        if (verificationToken.getType() != VerificationToken.TokenType.PASSWORD_RESET) {
            logger.warn("Invalid token type: {}", verificationToken.getType());
            return false;
        }
        
        return true;
    }
}