import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { userApi } from '../../api/userApi';
import { followApi } from '../../api/followApi';
import { artApi } from '../../api/artApi';
import { useNotification } from '../../contexts/NotificationContext';
import styles from './Profile.module.css';
import PFP from '../../assets/WA.jpg';
import artsIcon from '../../assets/arts-icon.svg';
import sms from '../../assets/message-icon.svg';
import ArtCard from '../../components/ArtCard/ArtCard';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';

export default function Profile() {
    const { userId } = useParams();
    const navigate = useNavigate();
    const { user: currentUser, isAuthenticated } = useAuth();
    const notification = useNotification();
    
    const [user, setUser] = useState(null);
    const [userArts, setUserArts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isSubscribed, setIsSubscribed] = useState(false);
    const [followerCount, setFollowerCount] = useState(0);
    const [followingCount, setFollowingCount] = useState(0);

    const getImageUrl = useCallback((imagePath) => {
        return artApi.utils?.getImageUrl?.(imagePath) || '/default-art.jpg';
    }, []);

    const isValidArt = (art) => {
        return art && art.id && (art.image || art.imageUrl) && (art.image !== 'string');
    };

    const loadUserData = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);
            
            let userData;
            if (isAuthenticated) {
                try {
                    userData = await userApi.getUserById(userId);
                } catch {
                    userData = await userApi.getPublicUser(userId);
                }
            } else {
                userData = await userApi.getPublicUser(userId);
            }
            
            if (!userData) {
                throw new Error('Пользователь не найден');
            }
            
            setUser(userApi.formatUser(userData));
            
            try {
                const artsData = await artApi.getArtsByAuthor(userId, 0, 30);
                const formattedArts = artsData?.content && Array.isArray(artsData.content) 
                    ? artsData.content 
                    : (Array.isArray(artsData) ? artsData : []);
                setUserArts(formattedArts);
            } catch (artsError) {
                console.error('Ошибка загрузки артов:', artsError);
                setUserArts([]);
            }
            
            try {
                const counts = await followApi.getFollowCounts(userId);
                setFollowerCount(counts?.followers ?? 0);
                setFollowingCount(counts?.following ?? 0);
            } catch (statsError) {
                console.error('Ошибка загрузки статистики:', statsError);
                setFollowerCount(0);
                setFollowingCount(0);
            }
            
            if (isAuthenticated && currentUser?.id && currentUser.id !== Number(userId)) {
                try {
                    const isFollowing = await followApi.isFollowing(userId);
                    setIsSubscribed(isFollowing);
                } catch (followError) {
                    console.error('Ошибка проверки подписки:', followError);
                }
            }
            
        } catch (err) {
            console.error('Ошибка загрузки профиля:', err);
            setError(err.message || 'Не удалось загрузить профиль');
        } finally {
            setLoading(false);
        }
    }, [userId, currentUser, isAuthenticated]);

    useEffect(() => {
        if (userId) {
            loadUserData();
        }
    }, [userId, loadUserData]);

    const handleSubscribe = async () => {
        if (!isAuthenticated) {
            navigate('/login');
            return;
        }
        
        try {
            if (isSubscribed) {
                await followApi.unfollow(userId);
                setIsSubscribed(false);
                const counts = await followApi.getFollowCounts(userId);
                setFollowerCount(counts?.followers ?? 0);
            } else {
                await followApi.follow(userId);
                setIsSubscribed(true);
                const counts = await followApi.getFollowCounts(userId);
                setFollowerCount(counts?.followers ?? 0);
            }
        } catch (error) {
            console.error('Ошибка подписки/отписки:', error);
            notification.error(`Ошибка: ${error.message}`, 3000);
        }
    };

    const handleMessage = () => {
        if (!isAuthenticated) {
            navigate('/login');
            return;
        }
        notification.warning('Функция сообщений в разработке', 3000);
    };

    if (loading) {
        return (
            <div className={styles.loading}>
                <div className={styles.spinner}></div>
                <span>Загрузка профиля...</span>
            </div>
        );
    }

    if (error) {
        return (
            <div className={styles.error}>
                <h2>Ошибка</h2>
                <p>{error}</p>
                <button 
                    onClick={() => navigate('/')}
                    className={styles.backButton}
                >
                    Вернуться на главную
                </button>
            </div>
        );
    }

    if (!user) {
        return (
            <div className={styles.notFound}>
                <h2>Пользователь не найден</h2>
                <p>Пользователь с ID {userId} не существует или его профиль скрыт</p>
                <button 
                    onClick={() => navigate('/')}
                    className={styles.backButton}
                >
                    Вернуться на главную
                </button>
            </div>
        );
    }

    const validArts = userArts.filter(isValidArt);
    const displayNameToShow = user.displayName || user.username;

    return (
        <>
            <div className={styles.headContent}>
                <div className={styles.faceName}>
                    <img 
                        src={user.avatarUrl || PFP} 
                        alt="profile-photo" 
                        className={styles.pfp}
                        onError={(e) => {
                            e.target.src = PFP;
                        }}
                    />
                </div>

                <div className={styles.contentWrapper}>
                    <div className={styles.headBg}></div>

                    <div className={styles.nameContainer}>
                        <span className={styles.displayName}>{displayNameToShow}</span>
                        <span className={styles.nickname}>@{user.username || 'user'}</span>
                    </div>

                    <div className={styles.headSFooter}>
                        <div className={styles.stats}>
                            <div className={styles.arts}>
                                <img src={artsIcon} alt="arts" />
                                <span>{` ${validArts.length}`}</span>
                            </div>
                            <span>Подписчики: {followerCount}</span>
                            <span>Подписки: {followingCount}</span>
                        </div>

                        {user.bio && (
                            <div className={styles.bio}>
                                <span>{user.bio}</span>
                            </div>
                        )}

                        <div className={styles.buttonsCover}>
                            <button 
                                className={styles.message}
                                onClick={handleMessage}
                                disabled={!isAuthenticated}
                                title={!isAuthenticated ? 'Войдите для отправки сообщений' : 'Написать сообщение'}
                            >
                                <img src={sms} alt="sms" className={styles.icon} />
                            </button>
                            <DefaultBtn
                                text={isSubscribed ? 'Подписка' : 'Подписаться'}
                                onClick={handleSubscribe}
                                className={`${styles.subscribe} ${isSubscribed ? styles.subscribed : ''}`}
                                disabled={!isAuthenticated}
                            />

                        </div> 

                    </div>
                </div>
            </div>
            
            <div className={styles.feed}>
                {validArts.length > 0 ? (
                    validArts.map(art => {
                        const imagePath = art.image || art.imageUrl;
                        const imageUrl = getImageUrl(imagePath);
                        
                        return (
                            <ArtCard 
                                key={art.id} 
                                id={art.id} 
                                image={imageUrl}
                                typeShow="full"
                                likesCount={art.likesCount || 0}
                                initialIsPrivate={art.isPublic === false}
                                title={art.title || 'Без названия'}
                                author={art.author || art.username || user.displayName}
                            />
                        );
                    })
                ) : (
                    <div className={styles.emptyState}>
                        <p>У пользователя пока нет публичных артов</p>
                    </div>
                )}
            </div>
        </>
    );
}