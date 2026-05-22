package com.example.artship.social.service;

import com.example.artship.social.model.Art;
import com.example.artship.social.model.Comment;
import com.example.artship.social.model.enumclass.ReportStatus;
import com.example.artship.social.model.enumclass.ReportTargetType;
import com.example.artship.social.model.mongo.Report;

import com.example.artship.social.repository.mongo.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    
    private final ReportRepository reportRepository;
    private final ArtService artService;
    private final CommentService commentService;
    
    public ReportService(ReportRepository reportRepository,
                        ArtService artService,
                        CommentService commentService
                    ) {
        this.reportRepository = reportRepository;
        this.artService = artService;
        this.commentService = commentService;
    }
    
    // Создание жалобы на арт
    @Transactional
    public Report createArtReport(Long reporterId, Long artId, String reason, String description) {
        logger.info("Creating report on art {} by user {}", artId, reporterId);
        
        if (reportRepository.existsByReporterIdAndTargetIdAndTargetType(reporterId, artId, ReportTargetType.ART)) {
            throw new RuntimeException("You have already reported this art");
        }
        
        Art art = artService.getArtById(artId)
                .orElseThrow(() -> new RuntimeException("Art not found"));
        
        Report report = new Report();
        report.setReporterId(reporterId);
        report.setTargetId(artId);
        report.setTargetType(ReportTargetType.ART);
        report.setReason(reason);
        report.setDescription(description);
        report.setStatus(ReportStatus.PENDING);
        
        report.setArtTitle(art.getTitle());
        if (art.getAuthor() != null) {
            report.setArtAuthorUsername(art.getAuthor().getUsername());
        }
        
        long reportCount = reportRepository.countByTargetIdAndTargetType(artId, ReportTargetType.ART);
        report.setPriority((int) Math.min(reportCount + 1, 5));
        
        Report saved = reportRepository.save(report);
        logger.info("Report created with id: {}", saved.getId());
        
        if (reportCount + 1 >= 10) {
            logger.warn("Art {} has 5+ reports, auto-hiding", artId);
            artService.hideArt(artId);
        }
        
        return saved;
    }
    
    // Создание жалобы на комментарий
    @Transactional
    public Report createCommentReport(Long reporterId, Long commentId, String reason, String description) {
        logger.info("Creating report on comment {} by user {}", commentId, reporterId);
        
        if (reportRepository.existsByReporterIdAndTargetIdAndTargetType(reporterId, commentId, ReportTargetType.COMMENT)) {
            throw new RuntimeException("You have already reported this comment");
        }
        
        Comment comment = commentService.getCommentEntityById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        Report report = new Report();
        report.setReporterId(reporterId);
        report.setTargetId(commentId);
        report.setTargetType(ReportTargetType.COMMENT);
        report.setReason(reason);
        report.setDescription(description);
        report.setStatus(ReportStatus.PENDING);
        
        report.setCommentText(comment.getText());
        if (comment.getUser() != null) {
            report.setCommentAuthorUsername(comment.getUser().getUsername());
        }
        
        long reportCount = reportRepository.countByTargetIdAndTargetType(commentId, ReportTargetType.COMMENT);
        report.setPriority((int) Math.min(reportCount + 1, 5));
        
        Report saved = reportRepository.save(report);
        logger.info("Comment report created with id: {}", saved.getId());
        
        if (reportCount + 1 >= 5) {
            logger.warn("Comment {} has 5+ reports, auto-hiding", commentId);
            commentService.hideComment(commentId);  // ← ИСПРАВЛЕНО: вызываем метод из CommentService
        }
        
        return saved;
    }
    
    // Получение всех жалоб (для админа)
    public Page<Report> getAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable);
    }
    
    // Получение жалоб по статусу
    public Page<Report> getReportsByStatus(ReportStatus status, Pageable pageable) {
        return reportRepository.findByStatus(status, pageable);
    }
    
    // Обработка жалобы (админ)
    @Transactional
    public Report resolveReport(String reportId, String resolvedBy, String resolutionNote, boolean deleteContent) {
        logger.info("Resolving report {} by admin {}", reportId, resolvedBy);
        
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        
        report.setStatus(ReportStatus.RESOLVED);
        report.setResolvedBy(resolvedBy);
        report.setResolutionNote(resolutionNote);
        report.setResolvedAt(LocalDateTime.now());
        
        if (deleteContent) {
            if (report.getTargetType() == ReportTargetType.ART) {
                artService.forceDeleteArt(report.getTargetId());
                logger.info("Art {} deleted due to report", report.getTargetId());
            } else {
                commentService.deleteComment(report.getTargetId());
                logger.info("Comment {} deleted due to report", report.getTargetId());
            }
        }
        
        return reportRepository.save(report);
    }
    
    // Отклонение жалобы
    @Transactional
    public Report rejectReport(String reportId, String resolvedBy, String resolutionNote) {
        logger.info("Rejecting report {} by admin {}", reportId, resolvedBy);
        
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        
        report.setStatus(ReportStatus.REJECTED);
        report.setResolvedBy(resolvedBy);
        report.setResolutionNote(resolutionNote);
        report.setResolvedAt(LocalDateTime.now());
        
        return reportRepository.save(report);
    }

    public Report getReportById(String reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + reportId));
    }
        
    // Получение количества жалоб на контент
    public long getReportCount(Long targetId, ReportTargetType targetType) {
        return reportRepository.countByTargetIdAndTargetType(targetId, targetType);
    }
}