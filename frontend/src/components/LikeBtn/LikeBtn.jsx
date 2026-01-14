import { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { likeApi } from '../../api/likeApi';
import styles from './LikeBtn.module.css';
import HeartOutlineIcon from '../../assets/heart-outline.svg';
import HeartFilledIcon from '../../assets/heart-filled.svg';
import { formatNumber } from '../../utils/formatNumber';

export default function LikeBtn({ 
  typeShow = 'full',
  className, 
  amountLikes = 0,
  artId,
  onLikeChange
}) {
    const { user, isAuthenticated } = useAuth();
    const [isLiked, setIsLiked] = useState(false);
    const [likesCount, setLikesCount] = useState(amountLikes);
    const [isLoading, setIsLoading] = useState(false);
    const [isInitialized, setIsInitialized] = useState(false);

    useEffect(() => {
        if (!artId) {
            setIsInitialized(true);
            return;
        }

        const loadLikeStatus = async () => {
            try {
                const count = await likeApi.getLikeCountByArt(artId);
                setLikesCount(count);
                
                if (isAuthenticated && user?.id) {
                    const liked = await likeApi.isLiked(user.id, artId);
                    setIsLiked(liked);
                }
            } catch (error) {
                console.error('Ошибка загрузки лайков:', error);
                setLikesCount(amountLikes);
            } finally {
                setIsInitialized(true);
            }
        };
        
        loadLikeStatus();
    }, [artId, isAuthenticated, user?.id, amountLikes]);

    const toggleLike = async () => {
        if (!isAuthenticated || !user?.id || isLoading || !artId) {
            return;
        }
        
        try {
            setIsLoading(true);
            
            if (isLiked) {
                await likeApi.removeLike(user.id, artId);
                setIsLiked(false);
                setLikesCount(prev => Math.max(0, prev - 1));
                
                if (onLikeChange) {
                    onLikeChange(likesCount - 1);
                }
            } else {
                await likeApi.addLike(user.id, artId);
                setIsLiked(true);
                setLikesCount(prev => prev + 1);
                
                if (onLikeChange) {
                    onLikeChange(likesCount + 1);
                }
            }
        } catch (error) {
            console.error('Ошибка при лайке:', error);
        } finally {
            setIsLoading(false);
        }
    };

    if (!isInitialized && artId) {
        return (
            <div className={`${styles.likeBadge} ${className || ''}`}>
                <span className={styles.likesText}>...</span>
                <button
                    className={styles.heartContainer}
                    disabled
                >
                    <img
                        src={HeartOutlineIcon}
                        alt="загрузка"
                        className={styles.heartIcon}
                    />
                </button>
            </div>
        );
    }

    const formattedLikes = formatNumber(likesCount);

    const showText = typeShow === 'amount' || typeShow === 'full';
    const showHeart = typeShow === 'like' || typeShow === 'full';

    return (
        <div className={`${styles.likeBadge} ${className || ''}`}>
            {showText && (
                <span className={styles.likesText}>{formattedLikes}</span>
            )}
            
            {showHeart && (
                <button
                    className={styles.heartContainer}
                    onClick={toggleLike}
                    aria-pressed={isLiked}
                    disabled={isLoading || !isAuthenticated || !artId}
                    title={!isAuthenticated ? "Войдите, чтобы поставить лайк" : ""}
                >
                    <img
                        src={isLiked ? HeartFilledIcon : HeartOutlineIcon}
                        alt={isLiked ? "лайк поставлен" : "лайк не поставлен"}
                        className={styles.heartIcon}
                    />
                </button>
            )}
        </div>
    );
}