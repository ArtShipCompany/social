import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { userApi } from '../../api/userApi';
import { followApi } from '../../api/followApi';
import { artApi } from '../../api/artApi';
import { linksApi } from '../../api/linksApi';
import { useNotification } from '../../contexts/NotificationContext';

import styles from './Profile.module.css';

import PFP from '../../assets/WA.jpg';
import sms from '../../assets/message-icon.svg';

import ArtCard from '../../components/ArtCard/ArtCard';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import ProfileStats from '../../components/ProfileStats/ProfileStats';
import SocialLinks from '../../components/SocialLinks/SocialLinks';

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
    const [socialLinks, setSocialLinks] = useState([]);

    const getImageUrl = useCallback((imagePath) => {
        return artApi.utils?.getImageUrl?.(imagePath) || userApi.getFullUrl(imagePath) || PFP;
    }, []);

    const isValidArt = (art) => art?.id && (art.image || art.imageUrl) && art.image !== 'string';

    const loadUserData = useCallback(async () => {
        if (!userId) return;
        
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
            
            if (!userData) throw new Error('Пользователь не найден');
            
            setUser(userData);
            
            // Арты
            try {
                const artsData = await artApi.getArtsByAuthor(userId, 0, 30);
                const artsList = artsData?.content || artsData || [];
                setUserArts(Array.isArray(artsList) ? artsList : []);
            } catch {
                setUserArts([]);
            }
            
            // Статистика
            try {
                const counts = await followApi.getFollowCounts(userId);
                setFollowerCount(counts?.followers ?? 0);
                setFollowingCount(counts?.following ?? 0);
            } catch {
                setFollowerCount(0);
                setFollowingCount(0);
            }
            
            try {
                const linksData = await linksApi.getUserLinks(userId, true);
                setSocialLinks(linksData.links || []);
            } catch {
                setSocialLinks([]);
            }
            
            // Проверка подписки
            if (isAuthenticated && currentUser?.id && currentUser.id !== Number(userId)) {
                try {
                    const following = await followApi.isFollowing(userId);
                    setIsSubscribed(following);
                } catch {
                    setIsSubscribed(false);
                }
            }
            
        } catch (err) {
            console.error('Ошибка профиля:', err);
            setError(err.message || 'Не удалось загрузить');
        } finally {
            setLoading(false);
        }
    }, [userId, currentUser, isAuthenticated]);

    useEffect(() => {
        if (userId) loadUserData();
    }, [userId, loadUserData]);

    const handleSubscribe = async () => {
        if (!isAuthenticated) {
            navigate('/login');
            return;
        }
        
        try {
            if (isSubscribed) {
                await followApi.unfollow(userId);
            } else {
                await followApi.follow(userId);
            }
            setIsSubscribed(!isSubscribed);
            
            // Обновляем счётчик
            const counts = await followApi.getFollowCounts(userId);
            setFollowerCount(counts?.followers ?? 0);
        } catch (error) {
            console.error('Ошибка подписки:', error);
            notification.error('Не удалось изменить подписку', 3000);
        }
    };

    if (loading) {
        return (
            <div className={styles.loading}>
                <div className={styles.spinner}></div>
                <span>Загрузка...</span>
            </div>
        );
    }

    if (error || !user) {
        return (
            <div className={styles.error}>
                <h2>Ошибка</h2>
                <p>{error || 'Пользователь не найден'}</p>
                <button onClick={() => navigate('/')} className={styles.backButton}>На главную</button>
            </div>
        );
    }

    const validArts = userArts.filter(isValidArt);
    const isOwnProfile = currentUser?.id === user.id;

    return (
        <>
            <div className={styles.headContent}>
                <div className={styles.faceName}>
                    <img 
                        src={userApi.getFullUrl(user.avatarUrl) || PFP} 
                        alt="avatar" 
                        className={styles.pfp}
                        onError={(e) => { e.target.src = PFP; }}
                    />
                </div>

                <div className={styles.contentWrapper}>
                    <div className={styles.headBg}></div>
                    <div className={styles.nameContainer}>
                        <span className={styles.displayName}>{user.displayName || user.username}</span>
                        <span className={styles.nickname}>@{user.username}</span>
                    </div>

                    <div className={styles.headSFooter}>
                        <ProfileStats 
                            userId={user.id}
                            artsCount={validArts.length}
                            followersCount={followerCount}
                            followingCount={followingCount}
                        />
                        {user.bio && <div className={styles.bio}><span>{user.bio}</span></div>}

                        <SocialLinks links={socialLinks} />

                        <div className={styles.buttonsCover}>
                            <button 
                                className={styles.message}
                                onClick={() => notification.warning('Сообщения в разработке', 3000)}
                                disabled={!isAuthenticated}
                                title={!isAuthenticated ? 'Войдите' : 'Написать'}
                            >
                                <img src={sms} alt="message" className={styles.icon} />
                            </button>
                            
                            {!isOwnProfile && (
                                <DefaultBtn
                                    text={isSubscribed ? 'Подписка' : 'Подписаться'}
                                    onClick={handleSubscribe}
                                    className={`${styles.subscribe} ${isSubscribed ? styles.subscribed : ''}`}
                                    disabled={!isAuthenticated}
                                />
                            )}
                        </div> 
                    </div>
                </div>
            </div>
            
            <div className={styles.feed}>
                {validArts.length > 0 ? (
                    validArts.map(art => (
                        <ArtCard 
                            key={art.id} 
                            id={art.id} 
                            image={getImageUrl(art.image || art.imageUrl)}
                            typeShow="full"
                            likesCount={art.likesCount || 0}
                            initialIsPrivate={art.isPublicFlag === false}
                            title={art.title || 'Без названия'}
                            author={art.author?.username || user.username}
                        />
                    ))
                ) : (
                    <div className={styles.emptyState}>
                        <p>Нет публичных артов</p>
                    </div>
                )}
            </div>
        </>
    );
}