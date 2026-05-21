import { Link } from 'react-router-dom';
import { memo, useState, useCallback, useEffect } from 'react';
import styles from './CollectionCard.module.css';
import {collectionsApi, LIKED_COLLECTION_ID } from '../../api/collectionsApi';

import Delete from '../../assets/cross-delete.svg';
import Lock from '../../assets/lock-privacy.svg';
import Unlock from '../../assets/unlock-privacy.svg';

const CollectionCard = memo(function CollectionCard({ 
    id, 
    title = 'Без названия',
    coverImageUrl,
    artCount = 0,
    isPublic = true,
    isLikedCollection = false,
    showDeleteIcon = false,
    showPrivacyIcon = false,
    initialIsPrivate = false,
    onDelete, 
    onTogglePrivacy,
    onClick,
}) {
    const isPrivate = !isPublic;
    
    const [imgSrc, setImgSrc] = useState(null);
    const [imgError, setImgError] = useState(false);

    useEffect(() => {
        if (coverImageUrl) {
            setImgSrc(coverImageUrl);
        } else {
            setImgSrc('/default-collection-cover.png');
        }
        setImgError(false);
    }, [coverImageUrl]);

    const isSystemLikedCollection = 
        isLikedCollection || 
        collectionsApi.utils.isLikedCollection({ id });

    const handleDeleteClick = useCallback((e) => {
        e.preventDefault();
        e.stopPropagation();

        if (isSystemLikedCollection) {
            notification?.warning?.('Коллекцию "Мне понравилось" удалить нельзя');
            return;
        }
        onDelete?.(id);
    }, [id, onDelete, isSystemLikedCollection, notification]);

    const handlePrivacyClick = useCallback((e) => {
        e.preventDefault();
        e.stopPropagation();

        if (isSystemLikedCollection) {
            return;
        }

        onTogglePrivacy?.(id);
    }, [id, onTogglePrivacy, isSystemLikedCollection]);

    const handleImageError = useCallback(() => {
        setImgError(true);
    }, []);

    const collectionPath = isSystemLikedCollection
        ? '/collections/liked'
        : `/collections/${id}`;

    const displayArtCount = isSystemLikedCollection 
        ? (artCount ?? 0) 
        : artCount;

    return (
        <div className={styles.card}>
            <Link
                to={collectionPath}
                className={styles.imageContainer}
                onClick={() => onClick?.(id)}
            >
                {imgError ? (
                    <div className={styles.coverPlaceholder}>
                        <span>📁</span>
                    </div>
                ) : (
                    <img
                        src={imgSrc}
                        alt={title}
                        className={`${styles.coverImage} ${isPrivate ? styles.privateCover : ''}`}
                        onError={handleImageError}
                    />
                )}

                <div className={styles.overlay} />

                <div className={styles.content}>
                    <h3 className={styles.title}>{title}</h3>
                    <div className={styles.meta}>
                        <span className={styles.artCount}>
                            {displayArtCount} артов
                        </span>
                    </div>
                </div>

                {isSystemLikedCollection && (
                    <div className={styles.likedBadge}>
                        ❤️ Лайки
                    </div>
                )}

                {showDeleteIcon && !isSystemLikedCollection && (
                    <button
                        type="button"
                        className={`${styles.actionIcon} ${styles.deleteIcon}`}
                        onClick={handleDeleteClick}
                    >
                        <img src={Delete} alt="delete" />
                    </button>
                )}

                {showPrivacyIcon && !isSystemLikedCollection && (
                    <button
                        type="button"
                        className={`${styles.actionIcon} ${styles.privacyIcon}`}
                        onClick={handlePrivacyClick}
                    >
                        <img src={isPrivate ? Lock : Unlock} alt="privacy" />
                    </button>
                )}
            </Link>
        </div>
    );
});

export default CollectionCard;