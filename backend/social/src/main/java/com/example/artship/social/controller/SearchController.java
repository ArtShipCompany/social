package com.example.artship.social.controller;

import com.example.artship.social.dto.SearchResult;
import com.example.artship.social.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@Tag(name = "Search", description = "API для поиска контента и пользователей")
public class SearchController {
    
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    
    private final SearchService searchService;
    
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }
    @GetMapping("/smart")
    @Operation(summary = "Умный поиск")
    public ResponseEntity<SearchResult> smartSearch(
            @Parameter(description = "Поисковый запрос", required = true, 
                       example = "кошка")
            @RequestParam String query,
            
            @Parameter(description = "Лимит результатов")
            @RequestParam(defaultValue = "20") int limit) {
        
        logger.info("Smart search request: {}", query);
        
        SearchResult result = searchService.smartSearch(query, limit);
        return ResponseEntity.ok(result);
    }
    

    @GetMapping("/smart/paginated")
    @Operation(summary = "Умный поиск с пагинацией")
    public ResponseEntity<SearchResult> smartSearchPaginated(
            @Parameter(description = "Поисковый запрос", required = true)
            @RequestParam String query,
            
            @Parameter(description = "Страница для артов по заголовку")
            @RequestParam(defaultValue = "0") int artsTitlePage,
            
            @Parameter(description = "Страница для артов по тегам")
            @RequestParam(defaultValue = "0") int artsTagsPage,
            
            @Parameter(description = "Страница для пользователей")
            @RequestParam(defaultValue = "0") int usersPage,
            
            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable artsTitlePageable = PageRequest.of(artsTitlePage, size, Sort.by("createdAt").descending());
        Pageable artsTagsPageable = PageRequest.of(artsTagsPage, size, Sort.by("createdAt").descending());
        Pageable usersPageable = PageRequest.of(usersPage, size, Sort.by("createdAt").descending());
        
        SearchResult result = searchService.smartSearchPaginated(
            query, artsTitlePageable, artsTagsPageable, usersPageable);
        
        return ResponseEntity.ok(result);
    }
}