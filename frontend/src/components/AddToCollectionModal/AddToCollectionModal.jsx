import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { collectionsApi, LIKED_COLLECTION_ID } from '../../api/collectionsApi';
import { useNotification } from '../../contexts/NotificationContext';
import styles from './AddToCollectionModal.module.css';
import CloseIcon from '../../assets/cross-delete.svg';

export default function AddToCollectionModal({ isOpen, onClose, artId, onSuccess }) {
    const { user } = useAuth();
    const notification = useNotification();
    
    const [collections, setCollections] = useState([]);
    const [loading, setLoading] = useState(false);
    const [selectedCollectionId, setSelectedCollectionId] = useState(null);
    const [isAdding, setIsAdding] = useState(false);

    // Блокировка скролла фона при открытии модалки
    useEffect(() => {
        if (isOpen) {
            document.body.style.overflow = 'hidden';
        }
        return () => {
            document.body.style.overflow = '';
        };
    }, [isOpen]);

    useEffect(() => {
        if (isOpen && user?.id) {
            loadCollections();
        }
    }, [isOpen, user?.id]);

    const loadCollections = async () => {
        try {
            setLoading(true);
            const data = await collectionsApi.getUserCollections(user.id, { 
                page: 0, 
                size: 50,
                includeLiked: true 
            });
            
            const filtered = (data?.content || []).filter(col => {
                const id = String(col.id).toLowerCase();
                return id !== LIKED_COLLECTION_ID && 
                       id !== '__liked__' && 
                       id !== 'liked' &&
                       col.title !== 'Мне понравилось' &&
                       col.title !== 'Понравившиеся';
            });
            
            setCollections(filtered);
        } catch (error) {
            console.error('Ошибка загрузки коллекций:', error);
            notification.error('Не удалось загрузить коллекции');
        } finally {
            setLoading(false);
        }
    };

    const handleCollectionSelect = (collectionId) => {
        // Просто выбираем коллекцию, без добавления
        setSelectedCollectionId(collectionId);
    };

    const handleConfirmAdd = async () => {
        if (!selectedCollectionId) return;
        
        try {
            setIsAdding(true);
            await collectionsApi.addArtToCollection(selectedCollectionId, artId);
            
            notification.success('Арт добавлен в коллекцию!', 3000);
            onSuccess?.();
            handleClose();
        } catch (error) {
            console.error('Ошибка добавления в коллекцию:', error);
            notification.error(error.message || 'Не удалось добавить арт', 3000);
        } finally {
            setIsAdding(false);
        }
    };

    const handleClose = useCallback(() => {
        setSelectedCollectionId(null);
        onClose();
    }, [onClose]);

    const selectedCollection = collections.find(c => c.id === selectedCollectionId);

    if (!isOpen) return null;

    return (
        <div className={styles.modalOverlay} onClick={handleClose}>
            <div className={styles.modalContent} onClick={e => e.stopPropagation()}>
                <button className={styles.closeBtn} onClick={handleClose}>
                    <img src={CloseIcon} alt="Закрыть" />
                </button>
                
                <h3 className={styles.title}>Добавить в коллекцию</h3>
                
                {loading ? (
                    <div className={styles.loading}>
                        <div className={styles.spinner}></div>
                        <span>Загрузка коллекций...</span>
                    </div>
                ) : collections.length > 0 ? (
                    <>
                        {/* Скроллящийся список коллекций */}
                        <div className={styles.collectionsList}>
                            {collections.map(collection => (
                                <button
                                    key={collection.id}
                                    className={`${styles.collectionItem} ${
                                        selectedCollectionId === collection.id ? styles.selected : ''
                                    }`}
                                    onClick={() => handleCollectionSelect(collection.id)}
                                    disabled={isAdding}
                                >
                                    <div className={styles.collectionCover}>
                                        {collection.coverImageUrl ? (
                                            <img 
                                                src={collection.coverImageUrl} 
                                                alt={collection.title}
                                                onError={(e) => {
                                                    e.target.src = '/default-collection-cover.png';
                                                }}
                                            />
                                        ) : (
                                            <div className={styles.coverPlaceholder}>📁</div>
                                        )}
                                    </div>
                                    <div className={styles.collectionInfo}>
                                        <span className={styles.collectionTitle}>{collection.title}</span>
                                        <span className={styles.artCount}>
                                            {collection.artCount || 0} артов
                                        </span>
                                    </div>
                                    {selectedCollectionId === collection.id && (
                                        <span className={styles.checkmark}>✓</span>
                                    )}
                                </button>
                            ))}
                        </div>

                        {/* Кнопка подтверждения */}
                        <div className={styles.actionsFooter}>
                            <button
                                className={styles.cancelBtn}
                                onClick={handleClose}
                                disabled={isAdding}
                            >
                                Отмена
                            </button>
                            <button
                                className={`${styles.confirmBtn} ${!selectedCollectionId ? styles.disabled : ''}`}
                                onClick={handleConfirmAdd}
                                disabled={!selectedCollectionId || isAdding}
                            >
                                {isAdding ? 'Добавление...' : 'Добавить'}
                            </button>
                        </div>
                    </>
                ) : (
                    <div className={styles.emptyState}>
                        <span>У вас пока нет коллекций</span>
                    </div>
                )}
            </div>
        </div>
    );
}