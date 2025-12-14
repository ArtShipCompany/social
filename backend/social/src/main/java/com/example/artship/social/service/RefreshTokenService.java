package com.example.artship.social.service;

import com.example.artship.social.model.RefreshToken;
import com.example.artship.social.model.User;
import com.example.artship.social.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional
public class RefreshTokenService {
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    // Убрали PasswordEncoder для BCrypt
    
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();
    
    // Генерация случайного refresh token (безопасный)
    public String generateSecureRandomToken() {
        byte[] randomBytes = new byte[64]; // 512 бит
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
    
    // Хеширование с помощью SHA-256 (без ограничения длины)
    public String hashWithSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing token with SHA-256", e);
        }
    }
    
    // Конвертация байтов в hex строку
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    // Создание нового refresh token
    public RefreshToken createRefreshToken(User user, String deviceInfo, String ipAddress, String userAgent) {
        // Генерация случайного токена
        String rawToken = generateSecureRandomToken();
        
        // Хеширование токена с помощью SHA-256
        String tokenHash = hashWithSHA256(rawToken);
        
        // Проверяем, нет ли такого хеша уже в базе (крайне маловероятно, но на всякий случай)
        if (refreshTokenRepository.existsByTokenHash(tokenHash)) {
            throw new RuntimeException("Token hash collision detected");
        }
        
        // Создание объекта RefreshToken
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(
            LocalDateTime.now().plusDays(30) // 30 дней
        );
        refreshToken.setDeviceInfo(deviceInfo);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setUserAgent(userAgent);
        refreshToken.setRevoked(false);
        
        refreshTokenRepository.save(refreshToken);
        
        // Временно сохраняем raw token для возврата клиенту
        // ВАЖНО: raw token НЕ должен сохраняться в БД
        RefreshToken result = new RefreshToken();
        result.setId(refreshToken.getId());
        result.setTokenHash(rawToken); // Возвращаем raw token клиенту
        result.setUser(refreshToken.getUser());
        result.setExpiryDate(refreshToken.getExpiryDate());
        result.setIssuedAt(refreshToken.getIssuedAt());
        result.setDeviceInfo(refreshToken.getDeviceInfo());
        result.setIpAddress(refreshToken.getIpAddress());
        result.setUserAgent(refreshToken.getUserAgent());
        result.setRevoked(refreshToken.isRevoked());
        
        return result;
    }
    
    // Поиск refresh token по токену (клиент предоставляет raw token)
    public Optional<RefreshToken> findByToken(String token) {
        String tokenHash = hashWithSHA256(token);
        return refreshTokenRepository.findByTokenHash(tokenHash);
    }
    
    // Проверка валидности токена
    public boolean verifyToken(String token) {
        String tokenHash = hashWithSHA256(token);
        return refreshTokenRepository.findByTokenHash(tokenHash)
                .map(RefreshToken::isValid)
                .orElse(false);
    }
    
    // Отзыв токена
    @Transactional
    public void revokeToken(String token) {
        String tokenHash = hashWithSHA256(token);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                });
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public void revokeUserTokensExcept(Long userId, String exceptToken) {
        String exceptTokenHash = hashWithSHA256(exceptToken);
        refreshTokenRepository.findByUserId(userId).forEach(token -> {
            if (!token.getTokenHash().equals(exceptTokenHash)) {
                token.setRevoked(true);
            }
        });
    }
    

    @Scheduled(cron = "0 0 2 * * ?") 
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        logger.info("Expired refresh tokens cleaned up");
    }
    
    // Получение всех активных токенов пользователя
    public java.util.List<RefreshToken> getUserActiveTokens(Long userId) {
        return refreshTokenRepository.findByUserIdAndRevokedFalse(userId);
    }

    public boolean tokenExists(String token) {
        String tokenHash = hashWithSHA256(token);
        return refreshTokenRepository.existsByTokenHash(tokenHash);
    }
    

    @Transactional
    public void updateTokenDeviceInfo(String token, String deviceInfo, String ipAddress, String userAgent) {
        String tokenHash = hashWithSHA256(token);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(refreshToken -> {
                    if (deviceInfo != null) {
                        refreshToken.setDeviceInfo(deviceInfo);
                    }
                    if (ipAddress != null) {
                        refreshToken.setIpAddress(ipAddress);
                    }
                    if (userAgent != null) {
                        refreshToken.setUserAgent(userAgent);
                    }
                    refreshTokenRepository.save(refreshToken);
                });
    }
    
    // Продление срока действия токена
    @Transactional
    public void extendTokenExpiry(String token, int additionalDays) {
        String tokenHash = hashWithSHA256(token);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(refreshToken -> {
                    if (refreshToken.isValid()) {
                        refreshToken.setExpiryDate(
                            refreshToken.getExpiryDate().plusDays(additionalDays)
                        );
                        refreshTokenRepository.save(refreshToken);
                    }
                });
    }
    
    // Получение информации о токене (для админки)
    public Optional<RefreshToken> getTokenInfo(String token) {
        String tokenHash = hashWithSHA256(token);
        return refreshTokenRepository.findByTokenHash(tokenHash);
    }
    
    // Метод для тестирования хеширования
    public void testHashing(String testInput) {
        System.out.println("Testing SHA-256 hashing:");
        System.out.println("Input: " + testInput);
        System.out.println("Input length: " + testInput.length() + " characters");
        System.out.println("Input bytes (UTF-8): " + testInput.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
        
        String hash = hashWithSHA256(testInput);
        System.out.println("SHA-256 Hash: " + hash);
        System.out.println("Hash length: " + hash.length() + " characters");
        System.out.println("Hash (first 20 chars): " + hash.substring(0, Math.min(20, hash.length())));
        
        // Проверка совпадения
        String hash2 = hashWithSHA256(testInput);
        System.out.println("Hashes match: " + hash.equals(hash2));
    }
    
    // Логгер для отладки
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RefreshTokenService.class);
    
    // Метод для логирования информации о токене
    public void logTokenInfo(String token) {
        String tokenHash = hashWithSHA256(token);
        logger.debug("Token info - Raw length: {}, Hash: {}...", 
            token.length(), tokenHash.substring(0, Math.min(20, tokenHash.length())));
    }
}