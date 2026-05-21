package com.example.artship.social.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.artship.social.dto.ArtDto;
import com.example.artship.social.dto.TagDto;
import com.example.artship.social.dto.UserDto;
import com.example.artship.social.model.Art;
import com.example.artship.social.model.enumclass.ArtStatus;
import com.example.artship.social.model.User;
import com.example.artship.social.model.UserRole;
import com.example.artship.social.repository.ArtRepository;
import com.example.artship.social.repository.CollectionArtRepository;
import com.example.artship.social.repository.LikeRepository;
import com.example.artship.social.repository.UserRepository;

@Service
@Transactional
public class ArtService {
    private final ArtRepository artRepository;
    private final UserRepository userRepository;
    private final TagManagementService tagManagementService; 
    private final TagService tagService;
    private final LocalFileStorageService fileStorageService;
    private final LikeRepository likeRepository;
    private final CollectionArtRepository collectionArtRepository;
    private final CommentService commentService;

    public ArtService(ArtRepository artRepository, 
                     UserRepository userRepository,
                     TagManagementService tagManagementService, 
                     TagService tagService,
                     LocalFileStorageService fileStorageService, 
                     LikeRepository likeRepository,
                     CollectionArtRepository collectionArtRepository,
                     CommentService commentService) {
        this.artRepository = artRepository;
        this.userRepository = userRepository;
        this.tagManagementService = tagManagementService; 
        this.tagService = tagService;
        this.fileStorageService = fileStorageService;
        this.likeRepository = likeRepository;
        this.collectionArtRepository = collectionArtRepository;
        this.commentService = commentService;
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================
    
    /**
     * Проверка, виден ли арт для пользователя
     */
    private boolean isArtVisibleToUser(Art art, User viewer) {
        // Если арт удален или забанен - никто не видит
        if (art.getStatus() == ArtStatus.DELETED_BY_USER || 
            art.getStatus() == ArtStatus.BANNED) {
            return false;
        }
        
        // Если арт скрыт - видят только админ и модератор
        if (art.getStatus() == ArtStatus.HIDDEN) {
            return viewer != null && (viewer.getUserRole() == UserRole.ADMIN || 
                                     viewer.getUserRole() == UserRole.MODERATOR);
        }
        
        // Публичный арт видят все
        if (art.getIsPublicFlag() != null && art.getIsPublicFlag()) {
            return true;
        }
        
        // Приватный арт видят только автор, админ и модератор
        if (viewer == null) return false;
        
        return viewer.getId().equals(art.getAuthor().getId()) ||
               viewer.getUserRole() == UserRole.ADMIN ||
               viewer.getUserRole() == UserRole.MODERATOR;
    }
    
    /**
     * Получение Query с учетом статуса для публичных запросов
     */
    private Page<Art> getVisibleArts(Pageable pageable, User viewer) {
        if (viewer != null && (viewer.getUserRole() == UserRole.ADMIN || 
                              viewer.getUserRole() == UserRole.MODERATOR)) {
            // Админ и модератор видят все арты (кроме DELETED_BY_USER и BANNED)
            return artRepository.findByStatusNotIn(
                List.of(ArtStatus.DELETED_BY_USER, ArtStatus.BANNED), pageable);
        } else {
            // Обычные пользователи видят только ACTIVE и публичные
            return artRepository.findByStatusAndIsPublicFlagTrue(ArtStatus.ACTIVE, pageable);
        }
    }

    // ==================== CRUD ОПЕРАЦИИ ====================

    public Art save(Art art) {
        return artRepository.save(art);
    }

    public ArtDto createArt(Art art, Long userId) { 
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        art.setAuthor(author);
        art.setStatus(ArtStatus.ACTIVE);
        art.setCreatedAt(LocalDateTime.now());
        art.setUpdatedAt(LocalDateTime.now());
        
        Art savedArt = artRepository.save(art);
        return convertToDto(savedArt);
    }

    public ArtDto updateArt(Long id, Art artDetails) {
        Art art = artRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + id));
        
        art.setTitle(artDetails.getTitle());
        art.setDescription(artDetails.getDescription());
        art.setImageUrl(artDetails.getImageUrl());
        art.setProjectDataUrl(artDetails.getProjectDataUrl());
        art.setIsPublicFlag(artDetails.getIsPublicFlag());
        art.setUpdatedAt(LocalDateTime.now());
        
        Art updatedArt = artRepository.save(art);
        return convertToDto(updatedArt);
    }

    public void deleteArt(Long id) {
        Art art = artRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + id));
        
        String imageUrl = art.getImageUrl();
        if (imageUrl != null && imageUrl.startsWith("/api/files/images/")) {
            fileStorageService.deleteFile(imageUrl);
        }
        
        likeRepository.deleteByArtId(id);
        tagManagementService.removeAllTagsFromArt(id);
        artRepository.delete(art);
    }

    // ==================== GET ЗАПРОСЫ С УЧЕТОМ СТАТУСА ====================

    @Transactional(readOnly = true)
    public Optional<Art> getArtById(Long id) {
        return artRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<ArtDto> getArtDtoById(Long id, User viewer) {
        Optional<Art> artOpt = artRepository.findById(id);
        
        if (artOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Art art = artOpt.get();
        
        // Проверка видимости с учетом статуса
        if (!isArtVisibleToUser(art, viewer)) {
            return Optional.empty();
        }
        
        return Optional.of(convertToDto(art));
    }

    @Transactional(readOnly = true)
    public Page<Art> getPublicArts(Pageable pageable, User viewer) {
        return getVisibleArts(pageable, viewer);
    }

    @Transactional(readOnly = true)
    public Page<ArtDto> getPublicArtsDtos(Pageable pageable, User viewer) {
        return getVisibleArts(pageable, viewer).map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<ArtDto> getPublicArtDtosByAuthor(User author, Pageable pageable, User viewer) {
        Page<Art> artsPage;
        
        if (viewer != null && (viewer.getUserRole() == UserRole.ADMIN || 
                              viewer.getUserRole() == UserRole.MODERATOR)) {
            // Админ и модератор видят все арты автора (кроме DELETED_BY_USER и BANNED)
            artsPage = artRepository.findByAuthorAndStatusNotIn(
                author, List.of(ArtStatus.DELETED_BY_USER, ArtStatus.BANNED), pageable);
        } else {
            // Обычные пользователи видят только ACTIVE и публичные арты
            artsPage = artRepository.findByAuthorAndStatusAndIsPublicFlagTrue(
                author, ArtStatus.ACTIVE, pageable);
        }
        
        return artsPage.map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<ArtDto> getAllArtDtosByAuthor(User author, Pageable pageable, User viewer) {
        Page<Art> artsPage;
        
        // Свои арты видит автор, чужие - только с учетом прав
        if (viewer != null && viewer.getId().equals(author.getId())) {
            // Автор видит все свои арты (кроме DELETED_BY_USER - они удалены)
            artsPage = artRepository.findByAuthorAndStatusNotIn(
                author, List.of(ArtStatus.DELETED_BY_USER), pageable);
        } else {
            // Не автор - только видимые
            artsPage = getVisibleArts(pageable, viewer);
        }
        
        return artsPage.map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<Art> getUserFeed(Long userId, Pageable pageable, User viewer) {
        // Лента: арты от подписок + рекомендации, только ACTIVE и публичные
        return artRepository.findFeedByUserIdAndStatus(userId, ArtStatus.ACTIVE, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ArtDto> getUserFeedDtos(Long userId, Pageable pageable, User viewer) {
        return artRepository.findFeedByUserIdAndStatus(userId, ArtStatus.ACTIVE, pageable)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<Art> searchPublicArtsByTitle(String title, Pageable pageable, User viewer) {
        if (viewer != null && (viewer.getUserRole() == UserRole.ADMIN || 
                              viewer.getUserRole() == UserRole.MODERATOR)) {
            return artRepository.findByTitleContainingIgnoreCaseAndStatusNotIn(
                title, List.of(ArtStatus.DELETED_BY_USER, ArtStatus.BANNED), pageable);
        } else {
            return artRepository.findByTitleContainingIgnoreCaseAndStatusAndIsPublicFlagTrue(
                title, ArtStatus.ACTIVE, pageable);
        }
    }

    @Transactional(readOnly = true)
    public Page<ArtDto> searchPublicArtDtosByTitle(String title, Pageable pageable, User viewer) {
        return searchPublicArtsByTitle(title, pageable, viewer).map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<Art> findByTagName(String tagName, Pageable pageable, User viewer) {
        if (viewer != null && (viewer.getUserRole() == UserRole.ADMIN || 
                              viewer.getUserRole() == UserRole.MODERATOR)) {
            return artRepository.findByTagNameAndStatusNotIn(
                tagName, List.of(ArtStatus.DELETED_BY_USER, ArtStatus.BANNED), pageable);
        } else {
            return artRepository.findByTagNameAndStatusAndIsPublicFlagTrue(
                tagName, ArtStatus.ACTIVE, pageable);
        }
    }

    @Transactional(readOnly = true)
    public Page<ArtDto> findDtosByTagName(String tagName, Pageable pageable, User viewer) {
        return findByTagName(tagName, pageable, viewer).map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<Art> findByTagNames(List<String> tagNames, Pageable pageable, User viewer) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Page.empty(pageable);
        }
        
        if (viewer != null && (viewer.getUserRole() == UserRole.ADMIN || 
                              viewer.getUserRole() == UserRole.MODERATOR)) {
            return artRepository.findByTagNamesAndStatusNotIn(
                tagNames, tagNames.size(), List.of(ArtStatus.DELETED_BY_USER, ArtStatus.BANNED), pageable);
        } else {
            return artRepository.findByTagNamesAndStatusAndIsPublicFlagTrue(
                tagNames, tagNames.size(), ArtStatus.ACTIVE, pageable);
        }
    }
    
    @Transactional(readOnly = true)
    public Page<ArtDto> findDtosByTagNames(List<String> tagNames, Pageable pageable, User viewer) {
        return findByTagNames(tagNames, pageable, viewer).map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<Art> findByAnyTagNames(List<String> tagNames, Pageable pageable, User viewer) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Page.empty(pageable);
        }
        
        if (viewer != null && (viewer.getUserRole() == UserRole.ADMIN || 
                              viewer.getUserRole() == UserRole.MODERATOR)) {
            return artRepository.findByAnyTagNamesAndStatusNotIn(
                tagNames, List.of(ArtStatus.DELETED_BY_USER, ArtStatus.BANNED), pageable);
        } else {
            return artRepository.findByAnyTagNamesAndStatusAndIsPublicFlagTrue(
                tagNames, ArtStatus.ACTIVE, pageable);
        }
    }
    
    @Transactional(readOnly = true)
    public Page<ArtDto> findDtosByAnyTagNames(List<String> tagNames, Pageable pageable, User viewer) {
        return findByAnyTagNames(tagNames, pageable, viewer).map(this::convertToDto);
    }

    // ==================== МЕТОДЫ ДЛЯ ТЕГОВ ====================

    public ArtDto addTagsToArt(Long artId, List<String> tagNames) {
        tagManagementService.addTagsToArt(artId, tagNames);
        return getArtDtoById(artId, null)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + artId));
    }

    public ArtDto removeTagFromArt(Long artId, Long tagId) {
        tagManagementService.removeTagFromArt(artId, tagId);
        return getArtDtoById(artId, null)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + artId));
    }

    @Transactional(readOnly = true)
    public List<TagDto> getArtTags(Long artId) {
        List<TagDto> tags = tagManagementService.getTagsByArtId(artId);
        
        return tags.stream()
                .map(tag -> {
                    Long artCount = tagService.getArtCountByTagId(tag.getId());
                    tag.setArtCount(artCount != null ? artCount.intValue() : 0);
                    return tag;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isUserAuthorOfArt(Long artId, Long userId) {
        return artRepository.findById(artId)
                .map(art -> art.getAuthor() != null && art.getAuthor().getId().equals(userId))
                .orElse(false);
    }


    @Transactional
    public void deleteAllUserArts(Long userId) {
        List<Art> userArts = artRepository.findByAuthorId(userId);
        
        for (Art art : userArts) {
            boolean isInOtherCollections = collectionArtRepository.existsByArtIdAndUserIdNot(art.getId(), userId);
            
            if (isInOtherCollections) {
                art.setAuthor(null);  
                art.setIsPublicFlag(false);
                art.setStatus(ArtStatus.DELETED_BY_USER);
                art.setTitle("[Deleted User's Art]");
                art.setDescription(null);
                art.setUpdatedAt(LocalDateTime.now());
                artRepository.save(art);
            } else {
                likeRepository.deleteByArtId(art.getId());
                commentService.deleteAllCommentsByArtId(art.getId());
                tagManagementService.removeAllTagsFromArt(art.getId());
                
                if (art.getImageUrl() != null) {
                    fileStorageService.deleteFile(art.getImageUrl());
                }
                
                artRepository.delete(art);
            }
        }
    }
    

    @Transactional(readOnly = true)
    public Page<ArtDto> getArtsByStatus(ArtStatus status, Pageable pageable) {
        if (status != null) {
            return artRepository.findByStatus(status, pageable)
                    .map(this::convertToDto);
        } else {
            return artRepository.findAll(pageable)
                    .map(this::convertToDto);
        }
    }

    @Transactional(readOnly = true)
    public Map<ArtStatus, Long> getArtsStatusStatistics() {
        Map<ArtStatus, Long> statistics = new HashMap<>();
        for (ArtStatus status : ArtStatus.values()) {
            long count = artRepository.countByStatus(status);
            statistics.put(status, count);
        }
        return statistics;
    }
    
    @Transactional
    public void hideArt(Long artId) {
        Art art = artRepository.findById(artId)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + artId));
        
        art.setIsPublicFlag(false);
        art.setStatus(ArtStatus.HIDDEN);
        art.setUpdatedAt(LocalDateTime.now());
        
        artRepository.save(art);
    }

    @Transactional
    public void unhideArt(Long artId) {
        Art art = artRepository.findById(artId)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + artId));
        
        art.setIsPublicFlag(true);
        art.setStatus(ArtStatus.ACTIVE);
        art.setUpdatedAt(LocalDateTime.now());
        
        artRepository.save(art);
    }
    
    @Transactional
    public void banArt(Long artId) {
        Art art = artRepository.findById(artId)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + artId));
        
        art.setIsPublicFlag(false);
        art.setStatus(ArtStatus.BANNED);
        art.setUpdatedAt(LocalDateTime.now());
        
        artRepository.save(art);
    }
    
    @Transactional
    public void forceDeleteArt(Long artId) {
        Art art = artRepository.findById(artId)
                .orElseThrow(() -> new RuntimeException("Art not found"));
        
        collectionArtRepository.deleteByArtId(artId);
        likeRepository.deleteByArtId(artId);
        commentService.deleteAllCommentsByArtId(artId);
        tagManagementService.removeAllTagsFromArt(artId);
        
        if (art.getImageUrl() != null) {
            fileStorageService.deleteFile(art.getImageUrl());
        }
        
        artRepository.delete(art);
    }



    @Transactional(readOnly = true)
    public ArtDto convertToDto(Art art) {
        if (art == null) return null;
    
        ArtDto artDto = new ArtDto();
        artDto.setId(art.getId());
        artDto.setTitle(art.getTitle());
        artDto.setDescription(art.getDescription());
        artDto.setImage(art.getImageUrl());
        artDto.setProjectDataUrl(art.getProjectDataUrl());
        artDto.setPublicFlag(art.getIsPublicFlag() != null ? art.getIsPublicFlag() : true);
        artDto.setStatus(art.getStatus()); // Добавить status в DTO
        artDto.setCreatedAt(art.getCreatedAt());
        artDto.setUpdatedAt(art.getUpdatedAt());
    
        if (art.getAuthor() != null) {
            artDto.setAuthor(new UserDto(art.getAuthor()));
        }
    
        List<TagDto> tags = tagManagementService.getTagsByArtId(art.getId());
        artDto.setTags(tags);
    
        return artDto;
    }
}