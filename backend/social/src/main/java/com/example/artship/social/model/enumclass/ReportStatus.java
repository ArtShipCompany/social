package com.example.artship.social.model.enumclass;

public enum ReportStatus {
    PENDING,      // Ожидает рассмотрения
    REVIEWED,     // На рассмотрении
    REJECTED,     // Отклонена (жалоба необоснованна)
    RESOLVED      // Решена (контент удален или забанен)
}