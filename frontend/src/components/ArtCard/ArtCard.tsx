import React from 'react';
import styles from './ArtCard.module.css';

// Импортируем SVG-иконки (можно заменить на ваши файлы)
import HeartOutlineIcon from '../../assets/heart-outline.svg';
import HeartFilledIcon from '../../assets/heart-filled.svg';


export const ArtCard = () => (
    <div className={styles.card}>
        {/* в будущем <img></img>*/}
        <div className={styles.likeBadge}>
            <span className={styles.likesText}>1.1k</span>
            <button className={styles.heartContainer}>
                <img src={HeartOutlineIcon} alt="heart-outline" className={styles.heartIcon} />
            </button>
        </div>
    </div>
);


export default ArtCard;