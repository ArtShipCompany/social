import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { followApi } from '../../api/followApi';
import { useNotification } from '../../contexts/NotificationContext';
import DefaultBtn from '../DefaultBtn/DefaultBtn';
import styles from './UserCard.module.css';
import PFP from '../../assets/WA.jpg';

export default function UserCard({ 
    user, 
    isSubscribed = false, 
    onToggleSubscribe,
    className = '' 
}) {
    const navigate = useNavigate();
    const { user: currentUser, isAuthenticated } = useAuth();
    const notification = useNotification();

    const { 
        id, 
        username, 
        displayName, 
        avatarUrl, 
        isPublic = true
    } = user || {};

    const handleCardClick = (e) => {
        if (e.target.closest(`.${styles.actionBtn}`)) return;
        if (currentUser?.id === id) {
            navigate('/me');
        } else if (id) {
            navigate(`/profile/${id}`);
        }
    };

    const handleToggleSubscribe = async (e) => {
        e.stopPropagation();
        
        if (!isAuthenticated) {
            navigate('/login');
            return;
        }

        const newStatus = !isSubscribed;

        try {
            if (isSubscribed) {
                await followApi.unfollow(id);
                notification.success(`Вы отписались от @${username}`, 2000);
            } else {
                await followApi.follow(id);
                notification.success(`Вы подписались на @${username}`, 2000);
            }
            onToggleSubscribe?.(id, newStatus);
        } catch (err) {
            console.error('Ошибка переключения подписки:', err);
            notification.error(err.message || 'Не удалось выполнить действие', 3000);
        }
    };

    const avatarSrc = avatarUrl 
        ? (avatarUrl.startsWith('http') ? avatarUrl : `http://localhost:8081${avatarUrl}`)
        : PFP;

    const displayNameToShow = displayName || username || 'Пользователь';
    const isMe = currentUser?.id === id;

    return (
        <div 
            className={`${styles.card} ${className} ${isPublic ? '' : styles.private}`}
            onClick={handleCardClick}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => e.key === 'Enter' && handleCardClick(e)}
            aria-label={`Профиль ${displayNameToShow}`}
        >
            <div className={styles.pfpNames}>

                <div className={styles.avatarWrapper}>
                    <img 
                        src={avatarSrc} 
                        alt={displayNameToShow}
                        className={styles.avatar}
                        onError={(e) => { e.target.src = PFP; }}
                    />
                    {!isPublic && (
                        <span className={styles.lockBadge} title="Приватный аккаунт">
                            🔒
                        </span>
                    )}
                </div>


                <div className={styles.info}>
                    <div className={styles.names}>
                        <span className={styles.displayName}>{displayNameToShow}</span>
                        {isMe && <span className={styles.meBadge}>вы</span>}
                    </div>
                    <span className={styles.username}>@{username}</span>
                </div>
            </div>

            {!isMe && (
                <DefaultBtn
                    text={isSubscribed ? 'Подписка' : 'Подписаться'}
                    onClick={handleToggleSubscribe}
                    className={`${styles.subscribe} ${isSubscribed ? styles.subscribed : ''}`}
                    disabled={!isAuthenticated}
                />
            )}
        </div>
    );
}