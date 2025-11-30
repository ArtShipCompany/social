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
import com.example.artship.social.model.Art;
import com.example.artship.social.model.Tag;
import com.example.artship.social.model.User;
import com.example.artship.social.repository.ArtRepository;
import com.example.artship.social.repository.UserRepository;

@Service
@Transactional
public class ArtService {
    private final ArtRepository artRepository;
    private final ArtTagService artTagService;
    private final TagService tagService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public ArtService(ArtRepository artRepository, TagService tagService, 
                     ArtTagService artTagService, UserRepository userRepository,
                     FileStorageService fileStorageService) {
        this.artRepository = artRepository;
        this.tagService = tagService;
        this.artTagService = artTagService;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
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
        art.setIsPublic(artDetails.getIsPublic());
        art.setUpdatedAt(LocalDateTime.now());
        
        Art updatedArt = artRepository.save(art);
        return convertToDto(updatedArt);
    }

    public void deleteArt(Long id) {
        Art art = artRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + id));
        
        // Удаляем файл изображения перед удалением арта
        String imageUrl = art.getImageUrl();
        if (imageUrl != null && imageUrl.startsWith("/api/files/images/")) {
            fileStorageService.deleteFile(imageUrl);
        }
        
        artTagService.removeAllTagsFromArt(id);
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
        return artRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<ArtDto> getPublicArtsDtos(Pageable pageable) {
        return artRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<Art> getPublicArtsByAuthor(User author) {
        return artRepository.findByAuthorAndIsPublicTrueOrderByCreatedAtDesc(author);
    }

    @Transactional(readOnly = true)
    public List<ArtDto> getPublicArtDtosByAuthor(User author) {
        return artRepository.findByAuthorAndIsPublicTrueOrderByCreatedAtDesc(author)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
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

    @Transactional(readOnly = true)
    public List<Art> getAllArtsByAuthor(User author) {
        return artRepository.findByAuthorOrderByCreatedAtDesc(author);
    }

    @Transactional(readOnly = true)
    public List<ArtDto> getAllArtDtosByAuthor(User author) {
        return artRepository.findByAuthorOrderByCreatedAtDesc(author)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<Art> searchPublicArtsByTitle(String title, Pageable pageable) {
        return artRepository.findByTitleContainingIgnoreCaseAndIsPublicTrue(title, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ArtDto> searchPublicArtDtosByTitle(String title, Pageable pageable) {
        return artRepository.findByTitleContainingIgnoreCaseAndIsPublicTrue(title, pageable)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<Art> findByTagName(String tagName, Pageable pageable) {
        return artRepository.findByTagNameAndIsPublicTrue(tagName, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ArtDto> findDtosByTagName(String tagName, Pageable pageable) {
        return artRepository.findByTagNameAndIsPublicTrue(tagName, pageable)
                .map(this::convertToDto);
    }

    // Методы для работы с тегами
    public ArtDto addTagsToArt(Long artId, List<String> tagNames) {
        artTagService.addTagsToArt(artId, tagNames);
        return getArtDtoById(artId)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + artId));
    }

    public ArtDto removeTagFromArt(Long artId, Long tagId) {
        artTagService.removeTagFromArt(artId, tagId);
        return getArtDtoById(artId)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + artId));
    }

    @Transactional(readOnly = true)
    public List<TagDto> getArtTags(Long artId) {
        List<Tag> tags = artTagService.getTagsByArtId(artId);
        return tags.stream()
                .map(tag -> {
                    TagDto dto = new TagDto(tag);
                    Long artCount = tagService.getArtCountByTagId(tag.getId());
                    dto.setArtCount(artCount != null ? artCount.intValue() : 0);
                    return dto;
                })
                .collect(Collectors.toList());
    }


    public ArtDto convertToDto(Art art) {
        List<Tag> tags = artTagService.getTagsByArtId(art.getId());
        

        ArtDto artDto = new ArtDto(art, tags);
        
        return artDto;
    }
}