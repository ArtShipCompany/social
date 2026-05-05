package com.example.artship.social.repository.mongo;

import com.example.artship.social.model.enumclass.ReportStatus;
import com.example.artship.social.model.enumclass.ReportTargetType;
import com.example.artship.social.model.mongo.Report;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends MongoRepository<Report, String> {
    
    boolean existsByReporterIdAndTargetIdAndTargetType(Long reporterId, Long targetId, ReportTargetType targetType);
    
    // Получение жалоб по статусу
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);
    
    // Получение жалоб по типу контента
    Page<Report> findByTargetType(ReportTargetType targetType, Pageable pageable);
    
    // Получение всех жалоб на конкретный контент
    List<Report> findByTargetIdAndTargetType(Long targetId, ReportTargetType targetType);
    
    // Количество жалоб на контент
    long countByTargetIdAndTargetType(Long targetId, ReportTargetType targetType);
    
    // Поиск с фильтрацией
    @Query("{ $or: [ " +
           "{ 'reason': { $regex: ?0, $options: 'i' } }, " +
           "{ 'description': { $regex: ?0, $options: 'i' } } " +
           "] }")
    Page<Report> searchReports(String keyword, Pageable pageable);
}
