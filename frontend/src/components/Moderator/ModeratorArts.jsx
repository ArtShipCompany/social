import { useState, useEffect } from 'react';
import { useNotification } from '../../contexts/NotificationContext';
import { artApi } from '../../api/artApi';
import ArtsTable from '../ArtControl/ArtsTable';
import ArtsFilters from '../ArtControl/ArtsFilters';
import ArtsPagination from '../ArtControl/ArtsPagination';
import styles from './ModeratorArts.module.css';

function ModeratorArts({ isAdmin }) {
    const notification = useNotification();
    
    const [arts, setArts] = useState([]);
    const [loading, setLoading] = useState(true);
    
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(20);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [statusFilter, setStatusFilter] = useState('');
    
    const loadArts = async () => {
        try {
            setLoading(true);
            const response = await artApi.getArtsByStatus(statusFilter || null, page, size);
            setArts(response.content || []);
            setTotalPages(response.totalPages || 0);
            setTotalElements(response.totalElements || 0);
        } catch (error) {
            notification.error('Ошибка загрузки артов');
        } finally {
            setLoading(false);
        }
    };
    
    useEffect(() => {
        loadArts();
    }, [page, size, statusFilter]);
    
    const handleHide = async (artId) => {
        try {
            await artApi.hideArt(artId);
            notification.success('Арт скрыт');
            loadArts();
        } catch (error) {
            notification.error('Ошибка при скрытии арта');
        }
    };
    
    const handleUnhide = async (artId) => {
        try {
            await artApi.unhideArt(artId);
            notification.success('Арт восстановлен');
            loadArts();
        } catch (error) {
            notification.error('Ошибка при восстановлении арта');
        }
    };
    
    const handleBan = async (artId) => {
        if (!isAdmin) return;
        try {
            await artApi.banArt(artId);
            notification.success('Арт забанен');
            loadArts();
        } catch (error) {
            notification.error('Ошибка при бане арта');
        }
    };
    
    return (
        <div className={styles.moderatorArts}>
            <div className={styles.header}>
                <h1>Модерация артов</h1>
                <p>Управление статусами и видимостью артов</p>
            </div>
            
            <ArtsFilters 
                statusFilter={statusFilter}
                onStatusChange={setStatusFilter}
                size={size}
                onSizeChange={setSize}
            />
            
            <ArtsTable 
                arts={arts}
                loading={loading}
                isAdmin={isAdmin}
                onHide={handleHide}
                onUnhide={handleUnhide}
                onBan={handleBan}
            />
            
            <ArtsPagination 
                page={page}
                totalPages={totalPages}
                totalElements={totalElements}
                size={size}
                onPageChange={setPage}
            />
        </div>
    );
}

export default ModeratorArts;