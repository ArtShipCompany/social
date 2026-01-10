import styles from './ArtCard.module.css';
import LikeBtn from '../LikeBtn/LikeBtn';

export default function ArtCard({ showLikeButton = true }) {
    return (
        <div className={styles.card}>
            {/*<img> </img>*/}
            <div className={styles.likeBadge}>
                <span className={styles.likesText}>1.1k</span>

                {showLikeButton && (
                    <LikeBtn />
                )}
            </div>
        </div>
    )
}