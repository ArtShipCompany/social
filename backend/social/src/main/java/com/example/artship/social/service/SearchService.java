package com.example.artship.social.service;

import com.example.artship.social.dto.ArtDto;
import com.example.artship.social.dto.SearchResult;
import com.example.artship.social.dto.UserDto;
import com.example.artship.social.repository.ArtRepository;
import com.example.artship.social.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    
    
     //Умный поиск: распознает #теги и @юзеров
     
    public SearchResult smartSearch(String query, int limit) {
        logger.info("Smart search for: {}", query);
        
        if (query == null || query.trim().isEmpty()) {
            return new SearchResult();
        }
        
        String trimmedQuery = query.trim();
        Pageable pageable = PageRequest.of(0, limit);
        
        if (trimmedQuery.startsWith("#")) {
            String tagName = trimmedQuery.substring(1);
            logger.info("Searching by tag: {}", tagName);
            
            SearchResult result = new SearchResult();
            result.setSearchType("tag");
            
            var artsPage = artRepository.findByTagNameAndIsPublicFlagTrue(tagName, pageable);
            List<ArtDto> arts = artsPage.getContent().stream()
                    .map(artService::convertToDto)
                    .collect(Collectors.toList());
            
            result.setArtsByTags(arts);
            result.setTotalArtsByTags(artsPage.getTotalElements());
            
            return result;
        }
        
        if (trimmedQuery.startsWith("@")) {
            String username = trimmedQuery.substring(1);
            logger.info("Searching by username: {}", username);
            
            SearchResult result = new SearchResult();
            result.setSearchType("user");
            
            var usersPage = userRepository.findByUsernameContainingIgnoreCase(username, pageable);
            List<UserDto> users = usersPage.getContent().stream()
                    .map(user -> {
                        UserDto dto = new UserDto(user);
                        dto.setEmail(null); // Скрываем email
                        return dto;
                    })
                    .collect(Collectors.toList());
            
            result.setUsersByUsername(users);
            result.setTotalUsers(usersPage.getTotalElements());
            
            return result;
        }
        
        logger.info("General search: {}", trimmedQuery);
        
        SearchResult result = new SearchResult();
        result.setSearchType("general");
        
        var artsByTitlePage = artRepository.findByTitleContainingIgnoreCaseAndIsPublicFlagTrue(trimmedQuery, pageable);
        List<ArtDto> artsByTitle = artsByTitlePage.getContent().stream()
                .map(artService::convertToDto)
                .collect(Collectors.toList());
        
        var artsByTagsPage = artRepository.findByTagNameContainingIgnoreCaseAndIsPublicFlagTrue(trimmedQuery, pageable);
        List<ArtDto> artsByTags = artsByTagsPage.getContent().stream()
                .map(artService::convertToDto)
                .collect(Collectors.toList());
        
        var usersPage = userRepository.findByUsernameContainingIgnoreCase(trimmedQuery, pageable);
        List<UserDto> users = usersPage.getContent().stream()
                .map(user -> {
                    UserDto dto = new UserDto(user);
                    dto.setEmail(null);
                    return dto;
                })
                .collect(Collectors.toList());
        
        result.setArtsByTitle(artsByTitle);
        result.setArtsByTags(artsByTags);
        result.setUsersByUsername(users);
        result.setTotalArtsByTitle(artsByTitlePage.getTotalElements());
        result.setTotalArtsByTags(artsByTagsPage.getTotalElements());
        result.setTotalUsers(usersPage.getTotalElements());
        
        return result;
    }
    
    
     //Умный поиск с пагинацией для каждого типа
     
    public SearchResult smartSearchPaginated(String query, 
                                                   Pageable artsTitlePageable,
                                                   Pageable artsTagsPageable,
                                                   Pageable usersPageable) {
        logger.info("Smart search with pagination for: {}", query);
        
        if (query == null || query.trim().isEmpty()) {
            return new SearchResult();
        }
        
        String trimmedQuery = query.trim();
        
        if (trimmedQuery.startsWith("#")) {
            String tagName = trimmedQuery.substring(1);
            SearchResult result = new SearchResult();
            result.setSearchType("tag");
            
            var artsPage = artRepository.findByTagNameAndIsPublicFlagTrue(tagName, artsTagsPageable);
            result.setArtsByTags(artsPage.getContent().stream()
                    .map(artService::convertToDto)
                    .collect(Collectors.toList()));
            result.setTotalArtsByTags(artsPage.getTotalElements());
            
            return result;
        }
        
        if (trimmedQuery.startsWith("@")) {
            String username = trimmedQuery.substring(1);
            SearchResult result = new SearchResult();
            result.setSearchType("user");
            
            var usersPage = userRepository.findByUsernameContainingIgnoreCase(username, usersPageable);
            result.setUsersByUsername(usersPage.getContent().stream()
                    .map(user -> {
                        UserDto dto = new UserDto(user);
                        dto.setEmail(null);
                        return dto;
                    })
                    .collect(Collectors.toList()));
            result.setTotalUsers(usersPage.getTotalElements());
            
            return result;
        }
        
        SearchResult result = new SearchResult();
        result.setSearchType("general");
        
        var artsByTitlePage = artRepository.findByTitleContainingIgnoreCaseAndIsPublicFlagTrue(trimmedQuery, artsTitlePageable);
        result.setArtsByTitle(artsByTitlePage.getContent().stream()
                .map(artService::convertToDto)
                .collect(Collectors.toList()));
        result.setTotalArtsByTitle(artsByTitlePage.getTotalElements());
        
        var artsByTagsPage = artRepository.findByTagNameContainingIgnoreCaseAndIsPublicFlagTrue(trimmedQuery, artsTagsPageable);
        result.setArtsByTags(artsByTagsPage.getContent().stream()
                .map(artService::convertToDto)
                .collect(Collectors.toList()));
        result.setTotalArtsByTags(artsByTagsPage.getTotalElements());
        
        var usersPage = userRepository.findByUsernameContainingIgnoreCase(trimmedQuery, usersPageable);
        result.setUsersByUsername(usersPage.getContent().stream()
                .map(user -> {
                    UserDto dto = new UserDto(user);
                    dto.setEmail(null);
                    return dto;
                })
                .collect(Collectors.toList()));
        result.setTotalUsers(usersPage.getTotalElements());
        
        return result;
    }
}