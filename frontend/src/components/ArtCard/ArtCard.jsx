import { Link } from 'react-router-dom';
import styles from './ArtCard.module.css';
import LikeBtn from '../LikeBtn/LikeBtn';

export default function ArtCard({ id, image, typeShow }) {
  return (
      <div className={styles.card}>
        <Link to={`/art/${id}`} className={styles.imageContainer}>
          <img src={image} alt="art" className={styles.artImage} />
        </Link>
        <LikeBtn typeShow={typeShow} />
      </div>
  );
}