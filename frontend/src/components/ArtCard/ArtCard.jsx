import { Link } from 'react-router-dom';
import styles from './ArtCard.module.css';
import LikeBtn from '../LikeBtn/LikeBtn';

export default function ArtCard({ id, image, showLikeButton }) {
  return (
    <Link to={`/art/${id}`} className={styles.cardLink}>
      <div className={styles.card}>
        <div className={styles.imageContainer}>
          <img src={image} alt="art" className={styles.artImage} />
        </div>
        <LikeBtn showLikeButton={showLikeButton} />
      </div>
    </Link>
  );
}