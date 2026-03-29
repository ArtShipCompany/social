package com.example.artship.social.service;

import com.example.artship.social.model.SocialLink;
import com.example.artship.social.model.SocialPlatform;
import com.example.artship.social.model.User;
import com.example.artship.social.repository.SocialLinkRepository;
import com.example.artship.social.requests.SocialLinkRequest;
import com.example.artship.social.response.SocialLinkResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SocialLinkService {
    
    private static final Logger logger = LoggerFactory.getLogger(SocialLinkService.class);
    
    @Autowired
    private SocialLinkRepository socialLinkRepository;
    
    // Получить все ссылки пользователя
    public List<SocialLinkResponse> getUserSocialLinks(Long userId, boolean onlyVisible) {
        User user = new User(userId);
        
        List<SocialLink> links;
        if (onlyVisible) {
            links = socialLinkRepository.findByUserAndIsVisibleTrueOrderByDisplayOrderAsc(user);
        } else {
            links = socialLinkRepository.findByUserOrderByDisplayOrderAsc(user);
        }
        
        return links.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public SocialLinkResponse addSocialLink(Long userId, SocialLinkRequest request) {
        logger.info("Adding social link for user ID: {}", userId);
        
        User user = new User(userId);
        
        if (socialLinkRepository.existsByUserAndPlatform(user, request.getPlatform())) {
            throw new RuntimeException("Social link for platform " + 
                request.getPlatform().getDisplayName() + " already exists");
        }
        
        if (!validateUrl(request.getUrl(), request.getPlatform())) {
            throw new RuntimeException("Invalid URL for platform " + request.getPlatform().getDisplayName());
        }
        
        SocialLink socialLink = new SocialLink();
        socialLink.setUser(user);
        socialLink.setPlatform(request.getPlatform());
        socialLink.setUrl(request.getUrl());
        socialLink.setVisible(request.isVisible());
        socialLink.setDisplayOrder(request.getDisplayOrder());
        
        SocialLink saved = socialLinkRepository.save(socialLink);
        logger.info("Social link added successfully with ID: {}", saved.getId());
        
        return convertToResponse(saved);
    }
    
    // Обновить ссылку
    public SocialLinkResponse updateSocialLink(Long userId, Long linkId, SocialLinkRequest request) {
        logger.info("Updating social link ID: {} for user ID: {}", linkId, userId);
        
        SocialLink socialLink = socialLinkRepository.findById(linkId)
                .orElseThrow(() -> new RuntimeException("Social link not found"));
        
        // Проверяем, что ссылка принадлежит пользователю
        if (!socialLink.getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to update this link");
        }
        
        if (!validateUrl(request.getUrl(), request.getPlatform())) {
            throw new RuntimeException("Invalid URL for platform " + request.getPlatform().getDisplayName());
        }
        
        socialLink.setUrl(request.getUrl());
        socialLink.setVisible(request.isVisible());
        socialLink.setDisplayOrder(request.getDisplayOrder());
        
        if (socialLink.getPlatform() != request.getPlatform()) {
            if (socialLinkRepository.existsByUserAndPlatform(socialLink.getUser(), request.getPlatform())) {
                throw new RuntimeException("Social link for platform " + 
                    request.getPlatform().getDisplayName() + " already exists");
            }
            socialLink.setPlatform(request.getPlatform());
        }
        
        SocialLink updated = socialLinkRepository.save(socialLink);
        logger.info("Social link updated successfully");
        
        return convertToResponse(updated);
    }
    
    // Удалить ссылку
    public void deleteSocialLink(Long userId, Long linkId) {
        logger.info("Deleting social link ID: {} for user ID: {}", linkId, userId);
        
        SocialLink socialLink = socialLinkRepository.findById(linkId)
                .orElseThrow(() -> new RuntimeException("Social link not found"));
        
        if (!socialLink.getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to delete this link");
        }
        
        socialLinkRepository.delete(socialLink);
        logger.info("Social link deleted successfully");
    }
    
    // Обновить порядок отображения
    public void updateDisplayOrder(Long userId, List<Long> linkIds) {
        logger.info("Updating display order for user ID: {}", userId);
        
        for (int i = 0; i < linkIds.size(); i++) {
            Long linkId = linkIds.get(i);
            SocialLink socialLink = socialLinkRepository.findById(linkId)
                    .orElseThrow(() -> new RuntimeException("Social link not found: " + linkId));
            
            if (!socialLink.getUser().getId().equals(userId)) {
                throw new RuntimeException("You don't have permission to update this link");
            }
            
            socialLink.setDisplayOrder(i);
            socialLinkRepository.save(socialLink);
        }
        
        logger.info("Display order updated successfully");
    }
    
    // Массовое обновление ссылок
    @Transactional
    public List<SocialLinkResponse> updateAllSocialLinks(Long userId, List<SocialLinkRequest> requests) {
        logger.info("Updating all social links for user ID: {}", userId);
        
        User user = new User(userId);
        
        // Удаляем все существующие ссылки
        socialLinkRepository.deleteAllByUser(user);
        
        // Создаем новые
        List<SocialLink> newLinks = requests.stream()
                .map(request -> {
                    SocialLink link = new SocialLink();
                    link.setUser(user);
                    link.setPlatform(request.getPlatform());
                    link.setUrl(request.getUrl());
                    link.setVisible(request.isVisible());
                    link.setDisplayOrder(request.getDisplayOrder());
                    return link;
                })
                .collect(Collectors.toList());
        
        List<SocialLink> saved = socialLinkRepository.saveAll(newLinks);
        logger.info("All social links updated successfully. Total: {}", saved.size());
        
        return saved.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    // Валидация URL
    public boolean validateUrl(String url, SocialPlatform platform) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        // Базовая валидация URL
        if (!url.matches("^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?$")) {
            return false;
        }
             return true;
        }
    
    
    // Конвертация в Response DTO
    private SocialLinkResponse convertToResponse(SocialLink socialLink) {
        return new SocialLinkResponse(
            socialLink.getId(),
            socialLink.getPlatform(),
            socialLink.getUrl(),
            socialLink.isVisible(),
            socialLink.getDisplayOrder(),
            socialLink.getCreatedAt(),
            socialLink.getUpdatedAt()
        );
    }
}