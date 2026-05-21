package com.example.artship.social.controller;

import com.example.artship.social.model.enumclass.ReportStatus;
import com.example.artship.social.model.mongo.Report;
import com.example.artship.social.requests.ReportRequest;
import com.example.artship.social.security.CustomUserDetails;
import com.example.artship.social.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "API для управления жалобами")
public class ReportController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    
    private final ReportService reportService;
    
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }
        
    /**
     * Создание жалобы (доступно всем авторизованным пользователям)
     */
    @PostMapping
    @Operation(summary = "Создать жалобу", 
               description = "Пользователь может пожаловаться на арт или комментарий")
    public ResponseEntity<?> createReport(
            @Valid @RequestBody ReportRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        logger.info("=== СОЗДАНИЕ ЖАЛОБЫ ===");
        
        // Проверка авторизации
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }
        
        try {
            Report report;
            
            if ("ART".equalsIgnoreCase(request.getTargetType())) {
                report = reportService.createArtReport(
                    currentUser.getId(),
                    request.getTargetId(),
                    request.getReason(),
                    request.getDescription()
                );
                logger.info("Жалоба на арт {} создана пользователем {}", 
                           request.getTargetId(), currentUser.getUsername());
                
            } else if ("COMMENT".equalsIgnoreCase(request.getTargetType())) {
                report = reportService.createCommentReport(
                    currentUser.getId(),
                    request.getTargetId(),
                    request.getReason(),
                    request.getDescription()
                );
                logger.info("Жалоба на комментарий {} создана пользователем {}", 
                           request.getTargetId(), currentUser.getUsername());
                
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid target type. Must be 'ART' or 'COMMENT'"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Report created successfully");
            response.put("reportId", report.getId());
            response.put("status", report.getStatus());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            logger.error("Ошибка создания жалобы: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
        
    /**
     * Получение всех жалоб (только для администратора)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить все жалобы (только админ)", 
               description = "Возвращает список всех жалоб с пагинацией и фильтрацией")
    public ResponseEntity<Page<Report>> getAllReports(
            @Parameter(description = "Номер страницы (начиная с 0)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "Сортировка")
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            
            @Parameter(description = "Фильтр по статусу")
            @RequestParam(required = false) ReportStatus status) {
        
        logger.info("Администратор запрашивает список жалоб. Page: {}, Size: {}, Status: {}", 
                   page, size, status);
        
        Pageable pageable = parsePageable(page, size, sort);
        
        Page<Report> reports;
        if (status != null) {
            reports = reportService.getReportsByStatus(status, pageable);
            logger.info("Найдено {} жалоб со статусом {}", reports.getTotalElements(), status);
        } else {
            reports = reportService.getAllReports(pageable);
            logger.info("Найдено {} жалоб всего", reports.getTotalElements());
        }
        
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Получение жалоб по статусу (только для администратора)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить жалобы по статусу (только админ)")
    public ResponseEntity<Page<Report>> getReportsByStatus(
            @Parameter(description = "Статус жалобы", required = true)
            @PathVariable ReportStatus status,
            
            @Parameter(description = "Номер страницы (начиная с 0)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "Сортировка")
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        logger.info("Администратор запрашивает жалобы со статусом: {}", status);
        
        Pageable pageable = parsePageable(page, size, sort);
        Page<Report> reports = reportService.getReportsByStatus(status, pageable);
        
        logger.info("Найдено {} жалоб со статусом {}", reports.getTotalElements(), status);
        
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Получение статистики по жалобам (только для администратора)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить статистику по жалобам (только админ)")
    public ResponseEntity<Map<String, Object>> getReportStatistics() {
        
        logger.info("Администратор запрашивает статистику по жалобам");
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", reportService.getAllReports(Pageable.unpaged()).getTotalElements());
        
        for (ReportStatus status : ReportStatus.values()) {
            long count = reportService.getReportsByStatus(status, Pageable.unpaged()).getTotalElements();
            statistics.put(status.toString().toLowerCase(), count);
        }
        
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Получение конкретной жалобы по ID (только для администратора)
     */
    @GetMapping("/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить жалобу по ID (только админ)")
    public ResponseEntity<?> getReportById(@PathVariable String reportId) {
        
        logger.info("Администратор запрашивает жалобу с ID: {}", reportId);
        
        try {
            Report report = reportService.getReportById(reportId);
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            logger.warn("Жалоба с ID {} не найдена", reportId);
            return ResponseEntity.notFound().build();
        }
    }
        
    /**
     * Обработка жалобы (удаление контента) - для модераторов и администраторов
     */
    @PutMapping("/{reportId}/resolve")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @Operation(summary = "Обработать жалобу (модератор или админ)", 
               description = "Подтверждает жалобу и удаляет контент")
    public ResponseEntity<?> resolveReport(
            @Parameter(description = "ID жалобы", required = true)
            @PathVariable String reportId,
            
            @Parameter(description = "Причина обработки", required = true)
            @RequestParam String resolutionNote,
            
            @Parameter(description = "Удалить контент")
            @RequestParam(defaultValue = "true") boolean deleteContent,
            
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        logger.info("=== ОБРАБОТКА ЖАЛОБЫ {} ===", reportId);
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Report report = reportService.resolveReport(
                reportId, 
                currentUser.getUsername(), 
                resolutionNote, 
                deleteContent
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Report resolved successfully");
            response.put("reportId", report.getId());
            response.put("status", report.getStatus());
            response.put("contentDeleted", deleteContent);
            
            logger.info("Жалоба {} обработана пользователем {}", 
                       reportId, currentUser.getUsername());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Ошибка обработки жалобы: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Отклонение жалобы (контент остаётся) - для модераторов и администраторов
     */
    @PutMapping("/{reportId}/reject")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @Operation(summary = "Отклонить жалобу (модератор или админ)", 
               description = "Отклоняет жалобу, контент остаётся")
    public ResponseEntity<?> rejectReport(
            @Parameter(description = "ID жалобы", required = true)
            @PathVariable String reportId,
            
            @Parameter(description = "Причина отклонения", required = true)
            @RequestParam String resolutionNote,
            
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        logger.info("=== ОТКЛОНЕНИЕ ЖАЛОБЫ {} ===", reportId);
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Report report = reportService.rejectReport(reportId, currentUser.getUsername(), resolutionNote);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Report rejected successfully");
            response.put("reportId", report.getId());
            response.put("status", report.getStatus());
            
            logger.info("Жалоба {} отклонена пользователем {}", 
                       reportId, currentUser.getUsername());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Ошибка отклонения жалобы: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Массовая обработка жалоб (только для администратора)
     */
    @PutMapping("/bulk/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Массовая обработка жалоб (только админ)")
    public ResponseEntity<?> bulkResolveReports(
            @RequestBody List<String> reportIds,
            @RequestParam String resolutionNote,
            @RequestParam(defaultValue = "true") boolean deleteContent,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        
        logger.info("=== МАССОВАЯ ОБРАБОТКА {} ЖАЛОБ ===", reportIds.size());
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        int successCount = 0;
        int failedCount = 0;
        
        for (String reportId : reportIds) {
            try {
                reportService.resolveReport(reportId, currentUser.getUsername(), resolutionNote, deleteContent);
                successCount++;
            } catch (Exception e) {
                logger.error("Не удалось обработать жалобу {}: {}", reportId, e.getMessage());
                failedCount++;
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Bulk resolve completed");
        response.put("successCount", successCount);
        response.put("failedCount", failedCount);
        response.put("total", reportIds.size());
        
        logger.info("Массовая обработка завершена: успешно {}, ошибок {}", successCount, failedCount);
        
        return ResponseEntity.ok(response);
    }
    
    
    private Pageable parsePageable(int page, int size, String sortParam) {
        String[] sortParts = sortParam.split(",");
        if (sortParts.length == 2) {
            String field = sortParts[0];
            String direction = sortParts[1];
            Sort sort = "desc".equalsIgnoreCase(direction) 
                ? Sort.by(field).descending() 
                : Sort.by(field).ascending();
            return org.springframework.data.domain.PageRequest.of(page, size, sort);
        }
        return org.springframework.data.domain.PageRequest.of(page, size, Sort.by("createdAt").descending());
    }
}