package com.example.artship.social.repository.mongo;

import com.example.artship.social.model.mongo.VerificationToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends MongoRepository<VerificationToken, String> {
    
    Optional<VerificationToken> findByToken(String token);
    
    @Query("{ 'userId': ?0, 'type': ?1, 'used': false }")
    List<VerificationToken> findValidTokensByUserAndType(Long userId, VerificationToken.TokenType type);
    
    @Query("{ 'userId': ?0, 'type': ?1, 'used': false, 'expiryDate': { $gt: ?2 } }")
    List<VerificationToken> findActiveTokensByUserAndType(Long userId, VerificationToken.TokenType type, LocalDateTime now);
    
    void deleteByUserIdAndType(Long userId, VerificationToken.TokenType type);
    
    @Query("{ 'expiryDate': { $lt: ?0 } }")
    List<VerificationToken> findExpiredTokens(LocalDateTime now);
    
    void deleteByUserId(Long userId);
}