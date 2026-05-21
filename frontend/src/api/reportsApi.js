import { fetchWithErrorHandling, API_URL } from './authApi';

export const reportsApi = {
    // Получение всех жалоб
    getAllReports: async (page = 0, size = 20, status = '') => {
        let url = `${API_URL}/reports?page=${page}&size=${size}&sort=createdAt,desc`;
        if (status) {
            url += `&status=${status}`;
        }
        return fetchWithErrorHandling(url);
    },
    
    // Получение статистики
    getStatistics: async () => {
        return fetchWithErrorHandling(`${API_URL}/reports/statistics`);
    },
    
    // Обработка жалобы
    resolveReport: async (reportId, resolutionNote, deleteContent = true) => {
        return fetchWithErrorHandling(`${API_URL}/reports/${reportId}/resolve?resolutionNote=${encodeURIComponent(resolutionNote)}&deleteContent=${deleteContent}`, {
            method: 'PUT',
        });
    },
    
    // Отклонение жалобы
    rejectReport: async (reportId, resolutionNote) => {
        return fetchWithErrorHandling(`${API_URL}/reports/${reportId}/reject?resolutionNote=${encodeURIComponent(resolutionNote)}`, {
            method: 'PUT',
        });
    },
};