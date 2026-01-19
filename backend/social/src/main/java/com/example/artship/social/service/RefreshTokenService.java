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
    
    
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    public String generateSecureRandomToken() {
        byte[] randomBytes = new byte[64]; 
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public String hashWithSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing token with SHA-256", e);
        }
    }
    

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
    
    public RefreshToken createRefreshToken(User user, String deviceInfo, String ipAddress, String userAgent) {
        String rawToken = generateSecureRandomToken();
        String tokenHash = hashWithSHA256(rawToken);
        
        if (refreshTokenRepository.existsByTokenHash(tokenHash)) {
            throw new RuntimeException("Token hash collision detected");
        }
        
 
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
        

        RefreshToken result = new RefreshToken();
        result.setId(refreshToken.getId());
        result.setTokenHash(rawToken); 
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
    
    public Optional<RefreshToken> getTokenInfo(String token) {
        String tokenHash = hashWithSHA256(token);
        return refreshTokenRepository.findByTokenHash(tokenHash);
    }
    
    

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RefreshTokenService.class);
    

    public void logTokenInfo(String token) {
        String tokenHash = hashWithSHA256(token);
        logger.debug("Token info - Raw length: {}, Hash: {}...", 
            token.length(), tokenHash.substring(0, Math.min(20, tokenHash.length())));
    }
}