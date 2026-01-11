import { Link } from 'react-router-dom';
import styles from './ArtCard.module.css';
import LikeBtn from '../LikeBtn/LikeBtn';

import Delete from '../../assets/cross-delete.svg'
import Lock from '../../assets/lock-privacy.svg'
import Unlock from '../../assets/unlock-privacy.svg'

export default function ArtCard({ 
    id, 
    image,
    // countLikes, 
    typeShow, 
    showDeleteIcon = false, 
    showPrivacyIcon = false 
}) {

  const handleDeleteClick = (e) => {
        e.preventDefault();
        e.stopPropagation(); // чтобы не срабатывал Link
        alert(`Удалить арт ${id}?`);
        // Тут будет вызов API или dispatch
    };

    const handlePrivacyClick = (e) => {
        e.preventDefault();
        e.stopPropagation();
        alert(`Переключить приватность арта ${id}`);
        // Тут будет логика переключения приватности
    };

  return (
        <div className={styles.card}>
            <Link to={`/art/${id}`} className={styles.imageContainer}>
                <img src={image} alt="art" className={styles.artImage} />
                
                {showDeleteIcon && (
                    <button 
                        className={`${styles.actionIcon} ${styles.deleteIcon}`}
                        onClick={handleDeleteClick}
                        aria-label="Удалить"
                    >
                        <img src={Delete} alt="Удалить" />
                    </button>
                )}

                {showPrivacyIcon && (
                    <button 
                        className={`${styles.actionIcon} ${styles.privacyIcon}`}
                        onClick={handlePrivacyClick}
                        aria-label="Сделать приватным"
                    >
                        <img src={Lock} alt="Приватность" />
                    </button>
                )}
            </Link>
            {/* amountLikes={countLikes} */}
            <LikeBtn typeShow={typeShow} amountLikes={200} />
        </div>
    );
}