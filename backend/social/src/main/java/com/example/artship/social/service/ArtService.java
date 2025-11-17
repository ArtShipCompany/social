package com.example.artship.social.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.artship.social.dto.ArtDto;
import com.example.artship.social.model.Art;
import com.example.artship.social.model.User;
import com.example.artship.social.repository.ArtRepository;

@Service
public class ArtService {
    @Autowired
    private ArtRepository artRepository;

    public ArtService(ArtRepository artRepository){
        this.artRepository = artRepository;
    }

    public Art createArt(Art art){
        return artRepository.save(art);
    }

    public Art updateArt(Long id, Art artDetails) {
        Art art = artRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + id));
        
        art.setTitle(artDetails.getTitle());
        art.setDescription(artDetails.getDescription());
        art.setImageUrl(artDetails.getImageUrl());
        art.setProjectDataUrl(artDetails.getProjectDataUrl());
        art.setIsPublic(artDetails.getIsPublic());
        
        return artRepository.save(art);
    }

    @Transactional(readOnly = true)
    public Optional<Art> getArtById(Long id) {
        return artRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<ArtDto> getArtDtoById(Long id) {
        return artRepository.findById(id)
                .map(ArtDto::new);
    }

    public void deleteArt(Long id) {
        Art art = artRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + id));
        artRepository.delete(art);
    }

    public Page<Art> getPublicArts(Pageable pageable) {
        return artRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public List<Art> getPublicArtsByAuthor(User author) {
        return artRepository.findByAuthorAndIsPublicTrueOrderByCreatedAtDesc(author);
    }

    @Transactional(readOnly = true)
    public List<ArtDto> getPublicArtDtosByAuthor(User author) {
        return artRepository.findByAuthorAndIsPublicTrueOrderByCreatedAtDesc(author)
                .stream()
                .map(ArtDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<Art> getUserFeed(Long userId, Pageable pageable) {
        return artRepository.findFeedByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ArtDto> getUserFeedDtos(Long userId, Pageable pageable) {
        return artRepository.findFeedByUserId(userId, pageable)
                .map(ArtDto::new);
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
    public Page<Art> searchPublicArtsByTitle(String title, Pageable pageable) {
        return artRepository.findByTitleContainingIgnoreCaseAndIsPublicTrue(title, pageable);
    }



}
