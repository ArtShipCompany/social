package com.example.artship.social.service;

import java.time.LocalDateTime;
import java.util.List;
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
import com.example.artship.social.model.User;
import com.example.artship.social.repository.ArtRepository;
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

    public ArtService(ArtRepository artRepository, 
                     UserRepository userRepository,
                     TagManagementService tagManagementService, 
                     TagService tagService,
                     LocalFileStorageService fileStorageService, LikeRepository likeRepository) {
        this.artRepository = artRepository;
        this.userRepository = userRepository;
        this.tagManagementService = tagManagementService; 
        this.tagService = tagService;
        this.fileStorageService = fileStorageService;
        this.likeRepository = likeRepository;
    }

    public Art save(Art art) {
        return artRepository.save(art);
    }

    public ArtDto createArt(Art art, Long userId) { 
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        art.setAuthor(author);
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

    @Transactional(readOnly = true)
    public Optional<Art> getArtById(Long id) {
        return artRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<ArtDto> getArtDtoById(Long id) {
        return artRepository.findById(id)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<Art> getPublicArts(Pageable pageable) {
        return artRepository.findByIsPublicFlagTrueOrderByCreatedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<ArtDto> getPublicArtsDtos(Pageable pageable) {
        return artRepository.findByIsPublicFlagTrueOrderByCreatedAtDesc(pageable)
                .map(this::convertToDto);
    }

    public Page<ArtDto> getPublicArtDtosByAuthor1(User author, Pageable pageable) {
        Page<Art> artsPage = artRepository.findByAuthorAndIsPublicFlagTrue(author, pageable);
        return artsPage.map(this::convertToDto);
    }
  

    @Transactional(readOnly = true)
    public Page<Art> getUserFeed(Long userId, Pageable pageable) {
        return artRepository.findFeedByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ArtDto> getUserFeedDtos(Long userId, Pageable pageable) {
        return artRepository.findFeedByUserId(userId, pageable)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public boolean isUserAuthorOfArt(Long artId, Long userId) {
        return artRepository.findById(artId)
                .map(art -> art.getAuthor().getId().equals(userId))
                .orElse(false);
    }

    public Page<ArtDto> getPublicArtDtosByAuthor(User author, Pageable pageable) {
        Page<Art> artsPage = artRepository.findByAuthorAndIsPublicFlagTrue(author, pageable);
        return artsPage.map(this::convertToDto);
    }

    public Page<ArtDto> getAllArtDtosByAuthor(User author, Pageable pageable) {
        Page<Art> artsPage = artRepository.findByAuthor(author, pageable);
        return artsPage.map(this::convertToDto);
}
    @Transactional(readOnly = true)
    public Page<Art> searchPublicArtsByTitle(String title, Pageable pageable) {
        return artRepository.findByTitleContainingIgnoreCaseAndIsPublicFlagTrue(title, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ArtDto> searchPublicArtDtosByTitle(String title, Pageable pageable) {
        return artRepository.findByTitleContainingIgnoreCaseAndIsPublicFlagTrue(title, pageable)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<Art> findByTagName(String tagName, Pageable pageable) {
        return artRepository.findByTagNameAndIsPublicFlagTrue(tagName, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ArtDto> findDtosByTagName(String tagName, Pageable pageable) {
        return artRepository.findByTagNameAndIsPublicFlagTrue(tagName, pageable)
                .map(this::convertToDto);
    }

    // Методы для работы с тегами 
    public ArtDto addTagsToArt(Long artId, List<String> tagNames) {
        tagManagementService.addTagsToArt(artId, tagNames);
        return getArtDtoById(artId)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + artId));
    }

    public ArtDto removeTagFromArt(Long artId, Long tagId) {
        tagManagementService.removeTagFromArt(artId, tagId);
        return getArtDtoById(artId)
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
    public Page<Art> findByTagNames(List<String> tagNames, Pageable pageable) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Page.empty(pageable);
        }
        return artRepository.findByTagNamesAndIsPublicFlagTrue(tagNames, tagNames.size(), pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<ArtDto> findDtosByTagNames(List<String> tagNames, Pageable pageable) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Page.empty(pageable);
        }
        return artRepository.findByTagNamesAndIsPublicFlagTrue(tagNames, tagNames.size(), pageable)
                .map(this::convertToDto);
    }
    

    @Transactional(readOnly = true)
    public Page<Art> findByAnyTagNames(List<String> tagNames, Pageable pageable) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Page.empty(pageable);
        }
        return artRepository.findByAnyTagNamesAndIsPublicFlagTrue(tagNames, pageable);
    }
    
    /**
     * Поиск артов по нескольким тегам (DTO) - OR
     */
    @Transactional(readOnly = true)
    public Page<ArtDto> findDtosByAnyTagNames(List<String> tagNames, Pageable pageable) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Page.empty(pageable);
        }
        return artRepository.findByAnyTagNamesAndIsPublicFlagTrue(tagNames, pageable)
                .map(this::convertToDto);
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