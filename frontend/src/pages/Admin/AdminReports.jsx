import { useState, useEffect } from 'react';
import { useNotification } from '../../contexts/NotificationContext';
import { reportsApi } from '../../api/reportsApi';
import ReportsFilters from '../../components/Reports/ReportsFilters';
import ReportsTable from '../../components/Reports/ReportsTable';
import ReportsPagination from '../../components/Reports/ReportsPagination';
import styles from './AdminReports.module.css';

function AdminReports() {
    const notification = useNotification();
    
    const [reports, setReports] = useState([]);
    const [loading, setLoading] = useState(true);
    const [processing, setProcessing] = useState(null);
    
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(20);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [statusFilter, setStatusFilter] = useState('');
    

    const loadReports = async () => {
        try {
            setLoading(true);
            const response = await reportsApi.getAllReports(page, size, statusFilter);
            setReports(response.content);
            setTotalPages(response.totalPages);
            setTotalElements(response.totalElements);
        } catch (error) {
            notification.error('Ошибка загрузки жалоб');
        } finally {
            setLoading(false);
        }
    };
    
    
    useEffect(() => {
        loadReports();
    }, [page, size, statusFilter]);
    
    const handleResolve = async (reportId, deleteContent) => {
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
        }
    };
    
    
    const handleReject = async (reportId, resolutionNote) => {
        try {
            await reportsApi.rejectReport(reportId, resolutionNote);
            notification.success('Жалоба отклонена');
            loadReports(); // Перезагружаем список жалоб
        } catch (error) {
            console.error('Error rejecting report:', error);
            notification.error('Ошибка при отклонении жалобы');
        }
    };
    
    return (
        <div className={styles.adminReportsPage}>

            <div className={styles.header}>
                <h1>Управление жалобами</h1>
            </div>
            <ReportsFilters 
                statusFilter={statusFilter}
                onStatusChange={setStatusFilter}
                size={size}
                onSizeChange={setSize}
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

export default AdminReports;