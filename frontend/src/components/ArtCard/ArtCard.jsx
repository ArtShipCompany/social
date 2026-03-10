import { Link } from 'react-router-dom';
import { memo, useState, useCallback, useEffect } from 'react';
import styles from './ArtCard.module.css';
import LikeBtn from '../LikeBtn/LikeBtn';

import Delete from '../../assets/cross-delete.svg'
import Lock from '../../assets/lock-privacy.svg'
import Unlock from '../../assets/unlock-privacy.svg'

const ArtCard = memo(function ArtCard({ 
    id, 
    image,
    typeShow, 
    showDeleteIcon = false, 
    showPrivacyIcon = false,
    initialIsPrivate = false,
    onOpenConfirmModal, 
    onTogglePrivacy, 
    likesCount = 0,
    title = 'Без названия',
}) {
    const [isPrivate, setIsPrivate] = useState(initialIsPrivate);
    const [isLoading, setIsLoading] = useState(false);
    const [imgSrc, setImgSrc] = useState(null);
    const [imgError, setImgError] = useState(false);

    useEffect(() => {
        setIsPrivate(initialIsPrivate);
    }, [initialIsPrivate]);

    // Инициализация изображения
    useEffect(() => {
        if (image) {
            setImgSrc(image);
            setImgError(false);
        }
    }, [image, id]);

    const handleDeleteClick = useCallback((e) => {
        e.preventDefault();
        e.stopPropagation();
        if (onOpenConfirmModal) {
            onOpenConfirmModal(id);
        }
    }, [id, onOpenConfirmModal]);

    const handlePrivacyClick = useCallback((e) => {
        e.preventDefault();
        e.stopPropagation();
        
        if (onTogglePrivacy) {
            onTogglePrivacy(id);
        }

        setIsPrivate(prev => !prev);
    }, [id, onTogglePrivacy]);

    const handleImageError = useCallback((e) => {
        console.error(`Failed to load image for art ${id}:`, image);
        setImgError(true);
        if (image.includes('//api/files/')) {
            const alternativePath = image.replace('//api/files/', '/api/files/');
            e.target.src = alternativePath;
        } else {
            e.target.src = '/default-art.jpg';
        }
    }, [id, image]);

    const handleImageLoad = useCallback(() => {
        setImgError(false);
    }, []);

    return (
        <div className={styles.card}>
            <Link to={`/art/${id}`} className={styles.imageContainer}>
                {imgError ? (
                    <div className={styles.imagePlaceholder}>
                        <span>Изображение не загружено</span>
                    </div>
                ) : imgSrc ?(
                    <img 
                        src={imgSrc} 
                        alt={title} 
                        className={`${styles.artImage} ${isPrivate ? styles.privateImage : ''}`}
                        onError={handleImageError}
                        onLoad={handleImageLoad}
                        loading="lazy"
                    />
                ) : (
                    <div className={styles.imagePlaceholder}>
                        <span>Загрузка...</span>
                    </div>
                )}
                
                
                
                {showDeleteIcon && (
                    <button 
                        className={`${styles.actionIcon} ${styles.deleteIcon}`}
                        onClick={handleDeleteClick}
                        aria-label="Удалить"
                        disabled={isLoading}
                    >
                        <img src={Delete} alt="Удалить" />
                    </button>
                )}

                {showPrivacyIcon && (
                    <button 
                        className={`${styles.actionIcon} ${styles.privacyIcon}`}
                        onClick={handlePrivacyClick}
                        aria-label={isPrivate ? "Сделать публичным" : "Сделать приватным"}
                        disabled={isLoading}
                    >
                        <img 
                            src={isPrivate ? Lock : Unlock} 
                            alt={isPrivate ? "Приватный" : "Публичный"} 
                        />
                    </button>
                )}
            </Link>
        </div>
    );
});

export default ArtCard;