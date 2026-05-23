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
    const [processing, setProcessing] = useState(null);
    
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(5);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [statusFilter, setStatusFilter] = useState('PENDING');
    
    const [stats, setStats] = useState({
        all: 0,
        pending: 0,
        resolved: 0,
        rejected: 0
    });

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
    
    const loadStatistics = async () => {
        try {
            const [allRes, pendingRes, resolvedRes, rejectedRes] = await Promise.all([
                reportsApi.getAllReports(0, 1, ''),
                reportsApi.getAllReports(0, 1, 'PENDING'),
                reportsApi.getAllReports(0, 1, 'RESOLVED'),
                reportsApi.getAllReports(0, 1, 'REJECTED')
            ]);
            
            setStats({
                all: allRes.totalElements || 0,
                pending: pendingRes.totalElements || 0,
                resolved: resolvedRes.totalElements || 0,
                rejected: rejectedRes.totalElements || 0
            });
        } catch (error) {
            console.error('Error loading statistics:', error);
        }
    };
    
    useEffect(() => {
        loadReports();
        loadStatistics();
    }, [page, size, statusFilter]);
    
    const handleResolve = async (reportId, deleteContent) => {
        setProcessing(reportId);
        try {
            const resolutionNote = deleteContent 
                ? 'Жалоба подтверждена, контент удалён' 
                : 'Жалоба подтверждена, контент скрыт';
            
            await reportsApi.resolveReport(reportId, resolutionNote, deleteContent);
            notification.success(deleteContent ? 'Контент удалён' : 'Контент скрыт');
            loadReports();
            loadStatistics();
        } catch (error) {
            console.error('Error resolving report:', error);
            notification.error('Ошибка обработки жалобы');
        } finally {
            setProcessing(null);
        }
    };
    
    const handleReject = async (reportId, resolutionNote) => {
        setProcessing(reportId);
        try {
            await reportsApi.rejectReport(reportId, resolutionNote);
            notification.success('Жалоба отклонена');
            loadReports();
            loadStatistics();
        } catch (error) {
            console.error('Error rejecting report:', error);
            notification.error('Ошибка при отклонении жалобы');
        } finally {
            setProcessing(null);
        }
    };
    
    return (
        <div className={styles.moderatorReports}>
            <div className={styles.header}>
                <h1>Управление жалобами</h1>
                <span>Просмотр и обработка жалоб пользователей:</span>
            </div>
            
            <ReportsFilters 
                statusFilter={statusFilter}
                onStatusChange={setStatusFilter}
                stats={stats}
            />
            
            <ReportsTable 
                reports={reports}
                loading={loading}
                processing={processing}
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