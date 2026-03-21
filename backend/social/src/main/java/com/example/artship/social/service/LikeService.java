package com.example.artship.social.service;

import com.example.artship.social.dto.LikeDto;
import com.example.artship.social.model.Art;
import com.example.artship.social.model.Like;
import com.example.artship.social.model.User;
import com.example.artship.social.repository.ArtRepository;
import com.example.artship.social.repository.LikeRepository;
import com.example.artship.social.repository.UserRepository;
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
    
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final ArtRepository artRepository;
    
    public LikeService(LikeRepository likeRepository, UserRepository userRepository, ArtRepository artRepository) {
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
        this.artRepository = artRepository;
    }
    
    // Добавление лайка
    public LikeDto addLike(Long userId, Long artId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Art art = artRepository.findById(artId)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + artId));
        
        if (likeRepository.existsByUserIdAndArtId(userId, artId)) {
            throw new RuntimeException("Like already exists");
        }
        
        Like like = new Like(user, art);
        Like savedLike = likeRepository.save(like);
        return new LikeDto(savedLike);
    }
    
    // Удаление лайка
    public void removeLike(Long userId, Long artId) {
        likeRepository.deleteByUserIdAndArtId(userId, artId);
    }
    
    // Проверка существования лайка
    @Transactional(readOnly = true)
    public boolean isLiked(Long userId, Long artId) {
        return likeRepository.existsByUserIdAndArtId(userId, artId);
    }
    
    // Получение лайков арта (с пагинацией)
    @Transactional(readOnly = true)
    public Page<LikeDto> getLikesByArtId(Long artId, Pageable pageable) {
        Page<Like> likesPage = likeRepository.findByArtId(artId, pageable);
        
        List<LikeDto> dtos = likesPage.getContent().stream()
                .map(LikeDto::new)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, likesPage.getTotalElements());
    }
    

    @Transactional(readOnly = true)
    public Page<LikeDto> getLikesByUserId(Long userId, Pageable pageable) {
        Page<Like> likesPage = likeRepository.findByUserId(userId, pageable);
        
        List<LikeDto> dtos = likesPage.getContent().stream()
                .map(LikeDto::new)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, likesPage.getTotalElements());
    }
    
    // Методы без пагинации (для обратной совместимости)
    @Transactional(readOnly = true)
    public List<LikeDto> getLikesByArtId(Long artId) {
        return likeRepository.findByArtId(artId).stream()
                .map(LikeDto::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<LikeDto> getLikesByUserId(Long userId) {
        return likeRepository.findByUserId(userId).stream()
                .map(LikeDto::new)
                .collect(Collectors.toList());
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
}