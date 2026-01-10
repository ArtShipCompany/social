import React, { useState } from 'react';

import styles from './LikeBtn.module.css';
import HeartOutlineIcon from '../../assets/heart-outline.svg';
import HeartFilledIcon from '../../assets/heart-filled.svg';

export default function LikeBtn() {

    const [isLiked, setIsLiked] = useState(false);
    const toggleLike = () => {
        setIsLiked(!isLiked);
    };

    return(
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
    )
}