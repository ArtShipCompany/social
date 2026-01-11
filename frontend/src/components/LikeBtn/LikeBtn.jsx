import { useState } from 'react';

import styles from './LikeBtn.module.css';
import HeartOutlineIcon from '../../assets/heart-outline.svg';
import HeartFilledIcon from '../../assets/heart-filled.svg';

export default function LikeBtn({ 
  typeShow = 'full',
  className 
}) {

    const [isLiked, setIsLiked] = useState(false);
    const toggleLike = () => {
        setIsLiked(!isLiked);
    };

    const showText = typeShow === 'amount' || typeShow === 'full';
    const showHeart = typeShow === 'like' || typeShow === 'full';

    return (
        <div className={`${styles.likeBadge} ${className || ''}`}>
            {showText && (
                <span className={styles.likesText}>1.1k</span>
            )}
            
            {showHeart && (
                <button
                className={styles.heartContainer}
                onClick={toggleLike}
                aria-pressed={isLiked}
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
