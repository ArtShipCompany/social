import { useState } from 'react';
import ArtViewModal from '../ArtViewModal/ArtViewModal';
import styles from './ArtsTable.module.css';

function ArtsTable({ arts, loading, isAdmin, onHide, onRestore, onBan }) {
    const [showArtModal, setShowArtModal] = useState(false);
    const [selectedArtId, setSelectedArtId] = useState(null);
    
    const getStatusBadge = (status) => {
        switch(status) {
            case 'ACTIVE': return { className: styles.statusActive, text: 'Активен' };
            case 'HIDDEN': return { className: styles.statusHidden, text: 'Скрыт' };
            case 'BANNED': return { className: styles.statusBanned, text: 'Забанен' };
            default: return { className: styles.statusActive, text: status };
        }
    };
    
    const formatDate = (dateString) => {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleDateString('ru-RU', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };
    
    // Обработчик клика по строке арта
    const handleArtClick = (artId) => {
        setSelectedArtId(artId);
        setShowArtModal(true);
    };
    
    if (loading) {
        return (
            <div className={styles.loading}>
                <div className={styles.spinner}></div>
                <p>Загрузка артов...</p>
            </div>
        );
    }
    
    if (arts.length === 0) {
        return (
            <div className={styles.emptyState}>
                <h3>Арты не найдены</h3>
                <p>Нет артов, соответствующих выбранным критериям</p>
            </div>
        );
    }
    
    return (
        <>
            <div className={styles.tableWrapper}>
                <table className={styles.artsTable}>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Изображение</th>
                            <th>Название</th>
                            <th>Автор</th>
                            <th>Дата</th>
                            <th>Статус</th>
                            <th>Действия</th>
                        </tr>
                    </thead>
                    <tbody>
                        {arts.map((art) => {
                            const statusBadge = getStatusBadge(art.status);
                            return (
                                <tr 
                                    key={art.id} 
                                    className={styles.clickableRow}
                                    onClick={() => handleArtClick(art.id)}
                                >
                                    <td className={styles.idCell}>#{art.id}</td>
                                    <td className={styles.imageCell}>
                                        <img 
                                            src={art.imageUrl} 
                                            alt={art.title}
                                            className={styles.thumbnail}
                                            onError={(e) => e.target.src = '/default-art.jpg'}
                                            onClick={(e) => e.stopPropagation()}
                                        />
                                    </td>
                                    <td className={styles.titleCell}>{art.title}</td>
                                    <td className={styles.authorCell}>{art.author?.displayName || art.author?.username || '-'}</td>
                                    <td className={styles.dateCell}>{formatDate(art.createdAt)}</td>
                                    <td className={styles.statusCell}>
                                        <span className={`${styles.statusBadge} ${statusBadge.className}`}>
                                            {statusBadge.text}
                                        </span>
                                    </td>
                                    <td className={styles.actionsCell} onClick={(e) => e.stopPropagation()}>
                                        {art.status === 'ACTIVE' && (
                                            <button 
                                                onClick={() => onHide(art.id)} 
                                                className={styles.hideBtn}
                                                title="Скрыть арт"
                                            >
                                                Скрыть
                                            </button>
                                        )}
                                        
                                        {isAdmin && art.status !== 'BANNED' && (
                                            <button 
                                                onClick={() => onBan(art.id)} 
                                                className={styles.banBtn}
                                                title="Забанить арт"
                                            >
                                                Забанить
                                            </button>
                                        )}
                                        
                                        {(art.status === 'HIDDEN' || art.status === 'BANNED') && (
                                            <button 
                                                onClick={() => onRestore(art.id)} 
                                                className={art.status === 'BANNED' ? styles.unbanBtn : styles.unhideBtn}
                                                title={art.status === 'BANNED' ? "Разбанить арт" : "Восстановить арт"}
                                            >
                                                {art.status === 'BANNED' ? 'Разбанить' : 'Восстановить'}
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            </div>
            
            {/* Модальное окно с артом */}
            {showArtModal && (
                <ArtViewModal
                    artId={selectedArtId}
                    onClose={() => setShowArtModal(false)}
                />
            )}
        </>
    );
}

export default ArtsTable;