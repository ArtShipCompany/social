package com.example.artship.social.service;

import com.example.artship.social.dto.ArtDto;
import com.example.artship.social.dto.UnifiedSearchResult;
import com.example.artship.social.dto.UserDto;
import com.example.artship.social.repository.ArtRepository;
import com.example.artship.social.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    
    private final ArtRepository artRepository;
    private final UserRepository userRepository;
    private final ArtService artService;
    
    public SearchService(ArtRepository artRepository, 
                         UserRepository userRepository,
                         ArtService artService) {
        this.artRepository = artRepository;
        this.userRepository = userRepository;
        this.artService = artService;
    }
    
    /**
     * Базовый поиск артов и пользователей
     */
    public UnifiedSearchResult search(String query, int limitArts, int limitUsers) {
        logger.info("Searching for: {} (arts limit: {}, users limit: {})", query, limitArts, limitUsers);
        
        if (query == null || query.trim().isEmpty()) {
            return new UnifiedSearchResult(List.of(), List.of(), 0, 0);
        }
        
        String searchTerm = query.trim();
        
        // Поиск артов
        List<ArtDto> arts = artRepository
            .findByTitleContainingIgnoreCaseAndIsPublicFlagTrue(searchTerm, Pageable.ofSize(limitArts))
            .stream()
            .map(artService::convertToDto)
            .collect(Collectors.toList());
        
        // Поиск пользователей
        List<UserDto> users = userRepository
            .findByUsernameContainingIgnoreCase(searchTerm, Pageable.ofSize(limitUsers))
            .stream()
            .map(user -> {
                UserDto dto = new UserDto(user);
                dto.setEmail(null); // Скрываем email
                return dto;
            })
            .collect(Collectors.toList());
        
        long totalArts = artRepository.countByTitleContainingIgnoreCaseAndIsPublicFlagTrue(searchTerm);
        long totalUsers = userRepository.countByUsernameContainingIgnoreCase(searchTerm);
        
        return new UnifiedSearchResult(arts, users, totalArts, totalUsers);
    }
    
    
    
    /**
     * Поиск с пагинацией
     */
    public UnifiedSearchResult searchPaginated(String query, Pageable artsPageable, Pageable usersPageable) {
        logger.info("Searching with pagination for: {}", query);
        
        if (query == null || query.trim().isEmpty()) {
            return new UnifiedSearchResult(List.of(), List.of(), 0, 0);
        }
        
        String searchTerm = query.trim();
        
        var artsPage = artRepository
            .findByTitleContainingIgnoreCaseAndIsPublicFlagTrue(searchTerm, artsPageable);
        
        List<ArtDto> arts = artsPage.getContent().stream()
            .map(artService::convertToDto)
            .collect(Collectors.toList());
        
        var usersPage = userRepository
            .findByUsernameContainingIgnoreCase(searchTerm, usersPageable);
        
        List<UserDto> users = usersPage.getContent().stream()
            .map(user -> {
                UserDto dto = new UserDto(user);
                dto.setEmail(null);
                return dto;
            })
            .collect(Collectors.toList());
        
        return new UnifiedSearchResult(arts, users, artsPage.getTotalElements(), usersPage.getTotalElements());
    }
      


    

    public Map<String, Long> getSearchCounts(String query) {
        logger.info("Getting search counts for: {}", query);
        
        Map<String, Long> counts = new HashMap<>();
        
        if (query == null || query.trim().isEmpty()) {
            counts.put("arts", 0L);
            counts.put("users", 0L);
            return counts;
        }
        
        String searchTerm = query.trim();
        counts.put("arts", artRepository.countByTitleContainingIgnoreCaseAndIsPublicFlagTrue(searchTerm));
        counts.put("users", userRepository.countByUsernameContainingIgnoreCase(searchTerm));
        
        return counts;
    }
}