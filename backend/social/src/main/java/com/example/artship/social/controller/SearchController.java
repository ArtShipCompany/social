package com.example.artship.social.controller;

import com.example.artship.social.dto.ArtDto;
import com.example.artship.social.dto.UnifiedSearchResult;
import com.example.artship.social.service.ArtService;
import com.example.artship.social.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/search")
@Tag(name = "Search", description = "API для поиска контента и пользователей")
public class SearchController {
    
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    
    private final ArtService artService;
    private final SearchService searchService;

    public SearchController(SearchService searchService, ArtService artService) {
        this.searchService = searchService;
        this.artService = artService;
    }

    @GetMapping("/all")
    @Operation(summary = "Поиск с пагинацией", 
               description = "Ищет арты и пользователей с отдельной пагинацией для каждого типа")
    public ResponseEntity<UnifiedSearchResult> searchPaginated(
            @Parameter(description = "Поисковый запрос", required = true)
            @RequestParam String query,
            
            @Parameter(description = "Номер страницы для артов (0-первая)")
            @RequestParam(defaultValue = "0") int artsPage,
            
            @Parameter(description = "Размер страницы для артов")
            @RequestParam(defaultValue = "10") int artsSize,
            
            @Parameter(description = "Номер страницы для пользователей (0-первая)")
            @RequestParam(defaultValue = "0") int usersPage,
            
            @Parameter(description = "Размер страницы для пользователей")
            @RequestParam(defaultValue = "10") int usersSize,
            
            @Parameter(description = "Сортировка артов", example = "createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String artsSort,
            
            @Parameter(description = "Сортировка пользователей", example = "createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String usersSort) {
        
        Pageable artsPageable = parsePageable(artsPage, artsSize, artsSort);
        Pageable usersPageable = parsePageable(usersPage, usersSize, usersSort);
        
        UnifiedSearchResult result = searchService.searchPaginated(query, artsPageable, usersPageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/quick")
    @Operation(summary = "Быстрый поиск", 
               description = "Для автокомплита - возвращает по 5 результатов каждого типа")
    public ResponseEntity<UnifiedSearchResult> quickSearch(
            @Parameter(description = "Поисковый запрос", required = true)
            @RequestParam String query) {
        
        UnifiedSearchResult result = searchService.search(query, 5, 5);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/counts")
    @Operation(summary = "Получить количество результатов", 
               description = "Возвращает количество артов и пользователей по запросу")
    public ResponseEntity<Map<String, Long>> getSearchCounts(
            @Parameter(description = "Поисковый запрос", required = true)
            @RequestParam String query) {
        
        Map<String, Long> counts = searchService.getSearchCounts(query);
        return ResponseEntity.ok(counts);
    }
        
    
     // Поиск публичных артов по одному тегу
     
    @Operation(summary = "Получить арты по одному тегу")
    @GetMapping("/tag/{tagName}")
    public ResponseEntity<Page<ArtDto>> getArtsByTag(
            @Parameter(description = "Название тега", required = true) 
            @PathVariable String tagName,
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество элементов на странице", example = "20") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Сортировка", example = "createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        logger.info("Поиск артов по тегу: '{}'", tagName);
        
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<ArtDto> arts = artService.findDtosByTagName(tagName, pageable);
        
        logger.info("Найдено {} артов с тегом '{}'", arts.getTotalElements(), tagName);
        return ResponseEntity.ok(arts);
    }
    
   
     //Получить арты по нескольким тегам (AND - все теги должны быть)
     
    @Operation(summary = "Получить арты по нескольким тегам (AND)", 
               description = "Возвращает арты, у которых есть ВСЕ указанные теги")
    @GetMapping("/tags/and")
    public ResponseEntity<Page<ArtDto>> getArtsByTagsAnd(
            @Parameter(description = "Список тегов через запятую", required = true, example = "cat,art")
            @RequestParam String tags,
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество элементов на странице", example = "20") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Сортировка", example = "createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        List<String> tagNames = Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toList());
        
        logger.info("Поиск артов по тегам (AND): {}", tagNames);
        
        if (tagNames.isEmpty()) {
            return ResponseEntity.ok(Page.empty());
        }
        
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<ArtDto> arts = artService.findDtosByTagNames(tagNames, pageable);
        
        logger.info("Найдено {} артов с тегами {}", arts.getTotalElements(), tagNames);
        return ResponseEntity.ok(arts);
    }
    
    
     //Получить арты по нескольким тегам (OR - любой из тегов)
     
    @Operation(summary = "Получить арты по нескольким тегам (OR)", 
               description = "Возвращает арты, у которых есть ХОТЯ БЫ ОДИН из указанных тегов")
    @GetMapping("/tags/or")
    public ResponseEntity<Page<ArtDto>> getArtsByTagsOr(
            @Parameter(description = "Список тегов через запятую", required = true, example = "cat,art")
            @RequestParam String tags,
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество элементов на странице", example = "20") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Сортировка", example = "createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        List<String> tagNames = Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toList());
        
        logger.info("Поиск артов по тегам (OR): {}", tagNames);
        
        if (tagNames.isEmpty()) {
            return ResponseEntity.ok(Page.empty());
        }
        
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<ArtDto> arts = artService.findDtosByAnyTagNames(tagNames, pageable);
        
        logger.info("Найдено {} артов с любым из тегов {}", arts.getTotalElements(), tagNames);
        return ResponseEntity.ok(arts);
    }
    
 
     // Получить арты по нескольким тегам с выбором режима
   
    @Operation(summary = "Получить арты по нескольким тегам", 
               description = "mode=and - все теги, mode=or - любой из тегов")
    @GetMapping("/tags")
    public ResponseEntity<Page<ArtDto>> getArtsByTags(
            @Parameter(description = "Список тегов через запятую", required = true, example = "cat,art")
            @RequestParam String tags,
            @Parameter(description = "Режим поиска: and (все теги) или or (любой тег)", example = "and")
            @RequestParam(defaultValue = "and") String mode,
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Количество элементов на странице", example = "20") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Сортировка", example = "createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        List<String> tagNames = Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toList());
        
        logger.info("Поиск артов по тегам: {}, mode: {}", tagNames, mode);
        
        if (tagNames.isEmpty()) {
            return ResponseEntity.ok(Page.empty());
        }
        
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<ArtDto> arts;
        
        if ("and".equalsIgnoreCase(mode)) {
            arts = artService.findDtosByTagNames(tagNames, pageable);
        } else {
            arts = artService.findDtosByAnyTagNames(tagNames, pageable);
        }
        
        logger.info("Найдено {} артов", arts.getTotalElements());
        return ResponseEntity.ok(arts);
    }
    
    

    private Sort parseSort(String sortParam) {
        String[] sortParts = sortParam.split(",");
        if (sortParts.length == 2) {
            String field = sortParts[0];
            String direction = sortParts[1];
            return "desc".equalsIgnoreCase(direction) 
                ? Sort.by(field).descending() 
                : Sort.by(field).ascending();
        }
        return Sort.by("createdAt").descending();
    }
    

    private Pageable parsePageable(int page, int size, String sortParam) {
        return PageRequest.of(page, size, parseSort(sortParam));
    }
}