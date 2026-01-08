import React, { useState } from 'react';
import styles from './ArtCard.module.css';
import HeartOutlineIcon from '../../assets/heart-outline.svg';
import HeartFilledIcon from '../../assets/heart-filled.svg';

export default function ArtCard() {

    const [isLiked, setIsLiked] = useState(false);
    const toggleLike = () => {
        setIsLiked(!isLiked);
    };

    return (
        <div className={styles.card}>
            {/*<img> </img>*/}
            <div className={styles.likeBadge}>
                <span className={styles.likesText}>1.1k</span>

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
            </div>
        </div>
    )
}