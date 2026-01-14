import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { userApi } from '../../api/userApi';
import { followApi } from '../../api/followApi';
import { artApi } from '../../api/artApi';
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
    
    const [user, setUser] = useState(null);
    const [userArts, setUserArts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isSubscribed, setIsSubscribed] = useState(false);
    const [followerCount, setFollowerCount] = useState(0);
    const [followingCount, setFollowingCount] = useState(0);

    const getImageUrl = (imagePath) => {
        if (!imagePath) return '/default-art.jpg';
        
        if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
            return imagePath;
        }
        
        let finalPath = imagePath;
        
        if (imagePath.startsWith('/api/files/images/')) {
            const filename = imagePath.split('/').pop();
            finalPath = `/uploads/images/${filename}`;
        }
        else if (imagePath.startsWith('/uploads/')) {
            finalPath = imagePath;
        }
        else if (imagePath.startsWith('uploads/')) {
            finalPath = `/${imagePath}`;
        }
        else if (!imagePath.includes('/')) {
            finalPath = `/uploads/images/${imagePath}`;
        }
        
        return `http://localhost:8081${finalPath}`;
    };

    const isValidArt = (art) => {
        return art && art.id && (art.image || art.imageUrl) && (art.image !== 'string');
    };

    const loadUserData = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);
            
            // Загружаем данные пользователя
            let userData;
            if (isAuthenticated) {
                try {
                    userData = await userApi.getUserById(userId);
                } catch (error) {
                    userData = await userApi.getPublicUser(userId);
                }
            } else {
                userData = await userApi.getPublicUser(userId);
            }
            
            if (!userData) {
                throw new Error('Пользователь не найден');
            }
            
            setUser(userApi.formatUser(userData));
            
            // Загружаем арты пользователя
            try {
                const artsData = await artApi.getArtsByAuthor(userId);
                
                let formattedArts = [];
                
                if (artsData && artsData.content && Array.isArray(artsData.content)) {
                    formattedArts = artsData.content;
                } else if (artsData && Array.isArray(artsData)) {
                    formattedArts = artsData;
                }
                
                setUserArts(formattedArts || []);
                
            } catch (artsError) {
                console.error('Ошибка загрузки артов:', artsError);
                setUserArts([]);
            }
            
            // Загружаем статистику подписок
            try {
                const [followers, following] = await Promise.all([
                    followApi.getFollowerCount(userId).catch(() => 0),
                    followApi.getFollowingCount(userId).catch(() => 0)
                ]);
                
                setFollowerCount(followers);
                setFollowingCount(following);
            } catch (statsError) {
                console.error('Ошибка загрузки статистики:', statsError);
            }
            
            // Проверяем подписку текущего пользователя
            if (isAuthenticated && currentUser && currentUser.id !== userId) {
                try {
                    const isFollowing = await followApi.isCurrentUserFollowing(userId);
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
                setFollowerCount(prev => Math.max(0, prev - 1));
            } else {
                await followApi.follow(userId);
                setIsSubscribed(true);
                setFollowerCount(prev => prev + 1);
            }
        } catch (error) {
            console.error('Ошибка подписки/отписки:', error);
            alert(`Ошибка: ${error.message}`);
        }
    };

    const handleMessage = () => {
        if (!isAuthenticated) {
            navigate('/login');
            return;
        }
        alert('Функция сообщений в разработке');
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
                    <span className={styles.nickname}>@{user.username}</span>
                </div>

                <div className={styles.contentWrapper}>
                    <div className={styles.headBg}></div>

                    <div className={styles.buttonsCover}>
                        <DefaultBtn
                            text={isSubscribed ? 'Подписка' : 'Подписаться'}
                            onClick={handleSubscribe}
                            className={`${styles.subscribe} ${isSubscribed ? styles.subscribed : ''}`}
                            disabled={!isAuthenticated}
                        />
                        <button 
                            className={styles.message}
                            onClick={handleMessage}
                            disabled={!isAuthenticated}
                            title={!isAuthenticated ? 'Войдите для отправки сообщений' : 'Написать сообщение'}
                        >
                            <img src={sms} alt="sms" className={styles.icon} />
                        </button>
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
                                typeShow="showLikes"
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