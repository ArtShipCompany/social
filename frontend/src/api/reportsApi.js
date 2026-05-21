import { fetchWithErrorHandling, API_URL } from './authApi';

export const reportsApi = {
    // Создание жалобы на арт
    createArtReport: async (artId, reason, description) => {
        return fetchWithErrorHandling(`${API_URL}/reports`, {
            method: 'POST',
            body: JSON.stringify({
                targetId: artId,
                targetType: 'ART',
                reason: reason,
                description: description || ''
            })
        });
    },
    
    // Создание жалобы на комментарий
    createCommentReport: async (commentId, reason, description) => {
        return fetchWithErrorHandling(`${API_URL}/reports`, {
            method: 'POST',
            body: JSON.stringify({
                targetId: commentId,
                targetType: 'COMMENT',
                reason: reason,
                description: description || ''
            })
        });
    },
    
    // Получение всех жалоб
    getAllReports: async (page = 0, size = 20, status = '') => {
        let url = `${API_URL}/reports?page=${page}&size=${size}&sort=createdAt,desc`;
        if (status) {
            url += `&status=${status}`;
        }
        return fetchWithErrorHandling(url);
    },
    
    // Получение конкретной жалобы по ID
    getReportById: async (reportId) => {
        return fetchWithErrorHandling(`${API_URL}/reports/${reportId}`);
    },
    
    // Получение статистики
    getStatistics: async () => {
        return fetchWithErrorHandling(`${API_URL}/reports/statistics`);
    },
    
    // Обработка жалобы (подтверждение, удаление контента)
    resolveReport: async (reportId, resolutionNote, deleteContent = true) => {
        return fetchWithErrorHandling(`${API_URL}/reports/${reportId}/resolve?resolutionNote=${encodeURIComponent(resolutionNote)}&deleteContent=${deleteContent}`, {
            method: 'PUT',
        });
    },
    
    // Отклонение жалобы (контент остаётся)
    rejectReport: async (reportId, resolutionNote) => {
        return fetchWithErrorHandling(`${API_URL}/reports/${reportId}/reject?resolutionNote=${encodeURIComponent(resolutionNote)}`, {
            method: 'PUT',
        });
    },
    
    // Массовая обработка жалоб (только для админа)
    bulkResolveReports: async (reportIds, resolutionNote, deleteContent = true) => {
        return fetchWithErrorHandling(`${API_URL}/reports/bulk/resolve?resolutionNote=${encodeURIComponent(resolutionNote)}&deleteContent=${deleteContent}`, {
            method: 'PUT',
            body: JSON.stringify(reportIds)
        });
    },
    
    // Получение жалоб по статусу
    getReportsByStatus: async (status, page = 0, size = 20) => {
        return fetchWithErrorHandling(`${API_URL}/reports/status/${status}?page=${page}&size=${size}&sort=createdAt,desc`);
    }
};