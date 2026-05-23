import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { artApi } from '../../api/artApi';
import { useNotification } from '../../contexts/NotificationContext';
import styles from './ArtViewModal.module.css';

function ArtViewModal({ artId, onClose }) {
    const navigate = useNavigate();
    const notification = useNotification();
    const [art, setArt] = useState(null);
    const [loading, setLoading] = useState(true);
    const [imageLoaded, setImageLoaded] = useState(false);
    const [imageError, setImageError] = useState(false);
    
    useEffect(() => {
        const loadArt = async () => {
            try {
                setLoading(true);
                const data = await artApi.getArtById(artId);
                setArt(data);
            } catch (error) {
                console.error('Error loading art:', error);
                notification.error('Не удалось загрузить арт');
                onClose();
            } finally {
                setLoading(false);
            }
        };
        
        if (artId) {
            loadArt();
        }
    }, [artId, notification, onClose]);
    
    // Закрытие по Escape
    useEffect(() => {
        const handleEsc = (e) => {
            if (e.key === 'Escape') {
                onClose();
            }
        };
        document.addEventListener('keydown', handleEsc);
        return () => document.removeEventListener('keydown', handleEsc);
    }, [onClose]);
    
    // Закрытие при клике на оверлей
    const handleOverlayClick = (e) => {
        if (e.target === e.currentTarget) {
            onClose();
        }
    };
    
    const handleImageLoad = () => {
        setImageLoaded(true);
    };
    
    const handleImageError = () => {
        setImageError(true);
        setImageLoaded(true);
    };
    
    const getImageUrl = (imagePath) => {
        if (!imagePath) return '/default-art.jpg';
        if (imagePath.startsWith('http')) return imagePath;
        return `http://localhost:8081${imagePath}`;
    };
    
    const handleGoToArtPage = () => {
        onClose();
        navigate(`/art/${artId}`);
    };
    
    if (loading) {
        return (
            <div className={styles.overlay} onClick={handleOverlayClick}>
                <div className={styles.modal}>
                    <div className={styles.loadingContainer}>
                        <div className={styles.spinner}></div>
                        <p>Загрузка арта...</p>
                    </div>
                </div>
            </div>
        );
    }
    
    if (!art) {
        return null;
    }
    
    return (
        <div className={styles.overlay} onClick={handleOverlayClick}>
            <div className={styles.modal}>
                
                <div className={styles.modalContent}>
                    {/* Изображение */}
                    <div className={styles.imageContainer}>
                        {!imageLoaded && !imageError && (
                            <div className={styles.imagePlaceholder}>
                                <div className={styles.spinnerSmall}></div>
                            </div>
                        )}
                        <img
                            src={getImageUrl(art.imageUrl)}
                            alt={art.title}
                            className={`${styles.artImage} ${imageLoaded ? styles.visible : styles.hidden}`}
                            onLoad={handleImageLoad}
                            onError={handleImageError}
                            style={{ display: imageLoaded && !imageError ? 'block' : 'none' }}
                        />
                        {imageError && (
                            <div className={styles.imageError}>
                                <p>Не удалось загрузить изображение</p>
                            </div>
                        )}
                    </div>
                    
                    {/* Информация об арте */}
                    <div className={styles.infoContainer}>
                        <div className={styles.header}>
                            <div className={styles.h}>
                                <h2 className={styles.artTitle}>{art.title || 'Без названия'}</h2>
                                <div className={styles.meta}>
                                    <span className={styles.id}>ID: {art.id}</span>
                                    <span className={styles.date}>
                                        {new Date(art.createdAt).toLocaleDateString('ru-RU')}
                                    </span>
                                </div>
                            </div>
                            
                            <div className={styles.author}>
                                <span className={styles.authorLabel}>Автор:</span>
                                <span className={styles.authorName}>
                                    {art.author?.displayName || art.author?.username || 'Неизвестный автор'}
                                </span>
                            </div>
                            
                            {art.tags && (
                                <div className={styles.tags}>
                                    <span className={styles.tagsLabel}>Теги:</span>
                                    <div className={styles.tagsList}>
                                        {typeof art.tags === 'string' 
                                            ? art.tags.split(' ').map((tag, i) => (
                                                <span key={i} className={styles.tag}>{tag}</span>
                                            ))
                                            : Array.isArray(art.tags) && art.tags.map((tag, i) => (
                                                <span key={i} className={styles.tag}>#{tag}</span>
                                            ))
                                        }
                                    </div>
                                </div>
                            )}
                            
                            {art.description && (
                                <div className={styles.description}>
                                    <span className={styles.descriptionLabel}>Описание:</span>
                                    <p>{art.description}</p>
                                </div>
                            )}
                        </div>

                        
                        <div className={styles.buttons}>
                            <button className={styles.goToBtn} onClick={handleGoToArtPage}>
                                Открыть на полную страницу
                            </button>
                            <button className={styles.closeModalBtn} onClick={onClose}>
                                Закрыть
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default ArtViewModal;