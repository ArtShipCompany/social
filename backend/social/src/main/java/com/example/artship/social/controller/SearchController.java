package com.example.artship.social.controller;

import com.example.artship.social.dto.UnifiedSearchResult;
import com.example.artship.social.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }


    @GetMapping("/all")
    @Operation(summary = "Поиск артов и пользователей", 
               description = "Ищет арты по названию и пользователей по username")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Поиск выполнен успешно"),
        @ApiResponse(responseCode = "400", description = "Некорректный запрос")
    })
    public ResponseEntity<UnifiedSearchResult> searchAll(
            @Parameter(description = "Поисковый запрос", required = true, example = "cat")
            @RequestParam String query,
            
            @Parameter(description = "Максимальное количество артов", example = "10")
            @RequestParam(defaultValue = "10") int limitArts,
            
            @Parameter(description = "Максимальное количество пользователей", example = "10")
            @RequestParam(defaultValue = "10") int limitUsers) {
        
        UnifiedSearchResult result = searchService.search(query, limitArts, limitUsers);
        return ResponseEntity.ok(result);
    }




    @GetMapping("/paginated")
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


    private Pageable parsePageable(int page, int size, String sortParam) {
        String[] sortParts = sortParam.split(",");
        if (sortParts.length == 2) {
            String field = sortParts[0];
            String direction = sortParts[1];
            Sort sort = "desc".equalsIgnoreCase(direction) 
                ? Sort.by(field).descending() 
                : Sort.by(field).ascending();
            return PageRequest.of(page, size, sort);
        }
        return PageRequest.of(page, size, Sort.by("createdAt").descending());
    }
}