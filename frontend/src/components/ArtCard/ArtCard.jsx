import styles from './ArtCard.module.css';
import LikeBtn from '../LikeBtn/LikeBtn';

export default function ArtCard({ image, showLikeButton }) {
    return (
        <div className={styles.card}>
            <div className={styles.imageContainer}>
                <img 
                    src={image} 
                    alt="art" 
                    className={styles.artImage}
                />
            </div>
            <LikeBtn showLikeButton={showLikeButton}/>
        </div>
    );
}