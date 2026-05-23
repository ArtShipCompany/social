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
    const [size, setSize] = useState(5);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [statusFilter, setStatusFilter] = useState('');
    
    const [stats, setStats] = useState({
        all: 0,
        active: 0,
        hidden: 0,
        banned: 0
    });
    
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
    
    const loadStatistics = async () => {
        try {
            // Загружаем арты каждого статуса для подсчета
            const [allRes, activeRes, hiddenRes, bannedRes] = await Promise.all([
                artApi.getArtsByStatus(null, 0, 1),
                artApi.getArtsByStatus('ACTIVE', 0, 1),
                artApi.getArtsByStatus('HIDDEN', 0, 1),
                artApi.getArtsByStatus('BANNED', 0, 1)
            ]);
            
            setStats({
                all: allRes.totalElements || 0,
                active: activeRes.totalElements || 0,
                hidden: hiddenRes.totalElements || 0,
                banned: bannedRes.totalElements || 0
            });
        } catch (error) {
            console.error('Error loading statistics:', error);
        }
    };
    
    useEffect(() => {
        loadArts();
        loadStatistics();
    }, [page, size, statusFilter]);
    
    const handleHide = async (artId) => {
        try {
            await artApi.hideArt(artId);
            notification.success('Арт скрыт');
            loadArts();
            loadStatistics();
        } catch (error) {
            notification.error('Ошибка при скрытии арта');
        }
    };
    
    const handleRestore = async (artId) => {
        try {
            await artApi.unhideArt(artId); 
            notification.success('Арт восстановлен');
            loadArts();
            loadStatistics();
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
            loadStatistics();
        } catch (error) {
            notification.error('Ошибка при бане арта');
        }
    };
    
    return (
        <div className={styles.moderatorArts}>
            <div className={styles.header}>
                <h1>Модерация артов</h1>
                <span>Управление статусами и видимостью артов:</span>
            </div>
            
            <ArtsFilters 
                statusFilter={statusFilter}
                onStatusChange={setStatusFilter}
                stats={stats}
            />
            
            <ArtsTable 
                arts={arts}
                loading={loading}
                isAdmin={isAdmin}
                onHide={handleHide}
                onRestore={handleRestore}  
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