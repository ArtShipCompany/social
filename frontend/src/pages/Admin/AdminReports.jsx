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
    
    const handleResolve = async (reportId, deleteContent = true) => {
        try {
            setProcessing(reportId);
            await reportsApi.resolveReport(reportId, 'Жалоба подтверждена, контент удалён', deleteContent);
            notification.success('Жалоба обработана');
            loadReports();
        } catch (error) {
            notification.error('Ошибка обработки жалобы');
        } finally {
            setProcessing(null);
        }
    };
    
    const handleReject = async (reportId) => {
        try {
            setProcessing(reportId);
            await reportsApi.rejectReport(reportId, 'Жалоба отклонена');
            notification.success('Жалоба отклонена');
            loadReports();
            loadStatistics();
        } catch (error) {
            notification.error('Ошибка отклонения жалобы');
        } finally {
            setProcessing(null);
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