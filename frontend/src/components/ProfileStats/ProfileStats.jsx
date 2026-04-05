import { useNavigate } from 'react-router-dom';
import styles from './ProfileStats.module.css';
import artsIcon from '../../assets/arts-icon.svg';

export default function ProfileStats({ 
    userId, 
    artsCount = 0, 
    followersCount = 0, 
    followingCount = 0,
    className = '' 
}) {
    const navigate = useNavigate();

    const handleFollowersClick = () => {
        navigate(`/follows/${userId}?tab=subscribers`);
    };

    const handleFollowingClick = () => {
        navigate(`/follows/${userId}?tab=subscriptions`);
    };

    return (
        <div className={`${styles.stats} ${className}`}>
            <div className={styles.arts}>
                <img src={artsIcon} alt="Арты" />
                <span>{artsCount}</span>
            </div>
            
            <button 
                type="button"
                className={styles.clickableStat}
                onClick={handleFollowersClick}
                aria-label={`Подписчики: ${followersCount}`}
            >
                Подписчики: <span className={styles.count}>{followersCount}</span>
            </button>
            
            <button 
                type="button"
                className={styles.clickableStat}
                onClick={handleFollowingClick}
                aria-label={`Подписки: ${followingCount}`}
            >
                Подписки: <span className={styles.count}>{followingCount}</span>
            </button>
        </div>
    );
}