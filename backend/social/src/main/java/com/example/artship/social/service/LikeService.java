package com.example.artship.social.service;

import com.example.artship.social.dto.LikeDto;
import com.example.artship.social.model.Art;
import com.example.artship.social.model.ArtLikes;
import com.example.artship.social.model.User;
import com.example.artship.social.repository.ArtRepository;
import com.example.artship.social.repository.LikeRepository;
import com.example.artship.social.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LikeService {
    
    private static final Logger log = LoggerFactory.getLogger(LikeService.class);
    
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final ArtRepository artRepository;
    private final LikedArtService likedArtService;  
    
    public LikeService(LikeRepository likeRepository, 
                       UserRepository userRepository, 
                       ArtRepository artRepository,
                       LikedArtService likedArtService) {
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
        this.artRepository = artRepository;
        this.likedArtService = likedArtService;
    }
    
    // Добавление лайка
    public LikeDto addLike(Long userId, Long artId) {
        log.info("User {} likes art {}", userId, artId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Art art = artRepository.findById(artId)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + artId));
        
        if (likeRepository.existsByUserIdAndArtId(userId, artId)) {
            throw new RuntimeException("Like already exists");
        }
        
        ArtLikes like = new ArtLikes(user, art);
        ArtLikes savedLike = likeRepository.save(like);
        
        try {
            likedArtService.addArtToLiked(userId, artId);
            log.info("Art {} automatically added to user {} liked collection", artId, userId);
        } catch (RuntimeException e) {
            log.warn("Could not add art to liked collection: {}", e.getMessage());
        }
        
        return new LikeDto(savedLike);
    }
    
    // Удаление лайка
    public void removeLike(Long userId, Long artId) {
        log.info("User {} unlikes art {}", userId, artId);
        
        if (!likeRepository.existsByUserIdAndArtId(userId, artId)) {
            throw new RuntimeException("Like does not exist");
        }
        
        likeRepository.deleteByUserIdAndArtId(userId, artId);
        
        // 👇 АВТОМАТИЧЕСКИ УДАЛЯЕМ ИЗ "ПОНРАВИВШИХСЯ"
        try {
            likedArtService.removeArtFromLiked(userId, artId);
            log.info("Art {} automatically removed from user {} liked collection", artId, userId);
        } catch (RuntimeException e) {
            log.warn("Could not remove art from liked collection: {}", e.getMessage());
        }
    }
    
    // Проверка существования лайка
    @Transactional(readOnly = true)
    public boolean isLiked(Long userId, Long artId) {
        return likeRepository.existsByUserIdAndArtId(userId, artId);
    }
    
    // Получение лайков арта (с пагинацией)
    @Transactional(readOnly = true)
    public Page<LikeDto> getLikesByArtId(Long artId, Pageable pageable) {
        Page<ArtLikes> likesPage = likeRepository.findByArtId(artId, pageable);
        
        List<LikeDto> dtos = likesPage.getContent().stream()
                .map(LikeDto::new)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, likesPage.getTotalElements());
    }
    
    // Количество лайков арта
    @Transactional(readOnly = true)
    public Long getLikeCountByArtId(Long artId) {
        return likeRepository.countByArtId(artId);
    }
    
    // Количество лайков пользователя
    @Transactional(readOnly = true)
    public Long getLikeCountByUserId(Long userId) {
        return likeRepository.countByUserId(userId);
    }
    
    // Удаление всех лайков пользователя
    @Transactional
    public void deleteAllUserLikes(Long userId) {
        log.info("Deleting all likes for user ID: {}", userId);
        
        likeRepository.deleteByUserId(userId);
        
        try {
            likedArtService.deleteAllLikedArtsByUser(userId);
            log.info("Deleted liked collection for user ID: {}", userId);
        } catch (RuntimeException e) {
            log.warn("Could not delete liked collection: {}", e.getMessage());
        }
    }
}