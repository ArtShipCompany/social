import { useState, useEffect } from 'react';
import { useNotification } from '../../contexts/NotificationContext';
import { reportsApi } from '../../api/reportsApi';
import ReportsTable from '../Reports/ReportsTable';
import ReportsFilters from '../Reports/ReportsFilters';
import ReportsPagination from '../Reports/ReportsPagination';
import styles from './ModeratorReports.module.css';

function ModeratorReports({ isAdmin }) {
    const notification = useNotification();
    
    const [reports, setReports] = useState([]);
    const [loading, setLoading] = useState(true);
    
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(20);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [statusFilter, setStatusFilter] = useState('PENDING');
    

    const loadReports = async () => {
        try {
            setLoading(true);
            const response = await reportsApi.getAllReports(page, size, statusFilter);
            setReports(response.content || []);
            setTotalPages(response.totalPages || 0);
            setTotalElements(response.totalElements || 0);
        } catch (error) {
            notification.error('Ошибка загрузки жалоб');
        } finally {
            setLoading(false);
        }
    };
    
    useEffect(() => {
        loadReports();
    }, [page, size, statusFilter]);
    
    const handleResolve = async (reportId) => {
        try {
            await reportsApi.resolveReport(reportId, 'Жалоба подтверждена, контент удалён', true);
            notification.success('Жалоба обработана');
            loadReports();
            loadStatistics();
        } catch (error) {
            notification.error('Ошибка обработки жалобы');
        }
    };
    
    const handleReject = async (reportId) => {
        try {
            await reportsApi.rejectReport(reportId, 'Жалоба отклонена');
            notification.success('Жалоба отклонена');
            loadReports();
            loadStatistics();
        } catch (error) {
            notification.error('Ошибка отклонения жалобы');
        }
    };
    
    return (
        <div className={styles.moderatorReports}>
            <div className={styles.header}>
                <h1>Управление жалобами</h1>
                <p>Просмотр и обработка жалоб пользователей</p>
            </div>
            
            <ReportsFilters 
                statusFilter={statusFilter}
                onStatusChange={setStatusFilter}
                size={size}
                onSizeChange={setSize}
                isAdmin={isAdmin}
            />
            
            <ReportsTable 
                reports={reports}
                loading={loading}
                onResolve={handleResolve}
                onReject={handleReject}
            />
            
            <ReportsPagination 
                page={page}
                totalPages={totalPages}
                totalElements={totalElements}
                size={size}
                onPageChange={setPage}
            />
        </div>
    );
}

export default ModeratorReports;