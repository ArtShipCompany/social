package com.example.artship.social.repository;

import com.example.artship.social.model.RefreshToken;
import com.example.artship.social.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    
    List<RefreshToken> findByUserAndRevokedFalse(User user);
    
    List<RefreshToken> findByUser(User user);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = false")
    List<RefreshToken> findByUserIdAndRevokedFalse(@Param("userId") Long userId);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId")
    List<RefreshToken> findByUserId(@Param("userId") Long userId);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteExpiredTokens(LocalDateTime now);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    void deleteByUserId(Long userId);
    
    @Transactional
    @Modifying
    void deleteByTokenHash(String tokenHash);
    
    boolean existsByTokenHash(String tokenHash);
    

    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = false")
    long countActiveTokensByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.revoked = false")
    long countAllActiveTokens();
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.ipAddress = :ipAddress")
    List<RefreshToken> findByIpAddress(@Param("ipAddress") String ipAddress);
}