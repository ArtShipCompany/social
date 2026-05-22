import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { userApi } from '../../api/userApi';
import { followApi } from '../../api/followApi';
import { artApi } from '../../api/artApi';
import { linksApi } from '../../api/linksApi';
import { collectionsApi, LIKED_COLLECTION_ID } from '../../api/collectionsApi';
import { useNotification } from '../../contexts/NotificationContext';

import styles from './Profile.module.css';

import PFP from '../../assets/WA.jpg';
import sms from '../../assets/message-icon.svg';

import ArtCard from '../../components/ArtCard/ArtCard';
import CollectionCard from '../../components/CollectionCard/CollectionCard';
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
    const [userCollections, setUserCollections] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isSubscribed, setIsSubscribed] = useState(false);
    const [followerCount, setFollowerCount] = useState(0);
    const [followingCount, setFollowingCount] = useState(0);
    const [socialLinks, setSocialLinks] = useState([]);

    // === Tabs & Collection state ===
    const [activeTab, setActiveTab] = useState('arts');
    const [selectedCollectionId, setSelectedCollectionId] = useState(null);
    const [selectedCollectionName, setSelectedCollectionName] = useState(null);
    const [selectedCollectionDescription, setSelectedCollectionDescription] = useState(null);
    const [collectionArts, setCollectionArts] = useState([]);
    const [isLoadingCollectionArts, setIsLoadingCollectionArts] = useState(false);

    const getImageUrl = useCallback((imagePath) => {
        return artApi.utils?.getImageUrl?.(imagePath) || userApi.getFullUrl(imagePath) || PFP;
    }, []);

    const isValidArt = useCallback((art) => {
        return art?.id && (art.image || art.imageUrl) && art.image !== 'string';
    }, []);

    // Фильтр артов: показываем только публичные ИЛИ свои (если это свой профиль)
    const filterPublicArts = useCallback((arts) => {
        if (!arts) return [];
        return arts.filter(art => {
            const isPublic = art.isPublicFlag !== false;
            const isOwner = currentUser?.id && art.author?.id === currentUser.id;
            return isPublic || isOwner;
        });
    }, [currentUser]);

    const isSystemCollection = useCallback((collection) => {
        if (!collection) return false;
        const id = String(collection.id).toLowerCase();
        return id === LIKED_COLLECTION_ID || id === '__liked__' || id === 'liked' || collection.isVirtual === true;
    }, []);

    const loadUserData = useCallback(async () => {
        if (!userId) return;
        
        try {
            setLoading(true);
            setError(null);
            
            // Загрузка данных пользователя
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
            
            // Загрузка артов (только публичные для чужого профиля)
            try {
                const artsData = await artApi.getArtsByAuthor(userId, 0, 30);
                const artsList = artsData?.content || artsData || [];
                const filteredArts = filterPublicArts(artsList);
                setUserArts(filteredArts);
            } catch {
                setUserArts([]);
            }
            
            // Загрузка коллекций (только публичные для чужого профиля)
            try {
                const collectionsData = await collectionsApi.getUserPublicCollections(userId, { 
                    page: 0, 
                    size: 20 
                });
                let collectionsList = collectionsData?.content || [];
                
                // Фильтруем: только публичные коллекции (исключаем системные, если не свои)
                const isOwnProfile = currentUser?.id === Number(userId);
                collectionsList = collectionsList.filter(col => {
                    if (isSystemCollection(col)) return isOwnProfile; // системные только в своём профиле
                    return col.isPublic !== false; // только публичные
                });
                
                setUserCollections(collectionsList);
            } catch {
                setUserCollections([]);
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
            
            // Соц. ссылки
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
    }, [userId, currentUser, isAuthenticated, filterPublicArts, isSystemCollection]);

    // Загрузка артов внутри коллекции (только публичные)
    const loadCollectionArts = useCallback(async (collection) => {
        if (!collection?.id) return;
        
        try {
            setIsLoadingCollectionArts(true);
            
            // Для системной коллекции "лайки" — только если это свой профиль
            if (isSystemCollection(collection)) {
                if (currentUser?.id === Number(userId)) {
                    const artsData = await collectionsApi.getLikedArts({ page: 0, size: 50 });
                    const filtered = filterPublicArts(artsData?.content || []);
                    setCollectionArts(filtered);
                } else {
                    setCollectionArts([]); // чужие лайки не показываем
                }
                return;
            }
            
            const artsData = await collectionsApi.getArtsInCollection(collection.id, { 
                page: 0, 
                size: 50 
            });
            const rawArts = artsData?.content || [];
            const filteredArts = filterPublicArts(rawArts);
            
            setCollectionArts(filteredArts);
        } catch (error) {
            console.error('Ошибка загрузки артов коллекции:', error);
            notification.error('Не удалось загрузить арты коллекции');
            setCollectionArts([]);
        } finally {
            setIsLoadingCollectionArts(false);
        }
    }, [userId, currentUser, notification, filterPublicArts, isSystemCollection]);

    useEffect(() => {
        if (userId) loadUserData();
    }, [userId, loadUserData]);

    // Обработчики табов и коллекций
    const handleTabChange = useCallback((tab) => {
        setActiveTab(tab);
        if (tab === 'collections') {
            handleBackToCollections();
        }
    }, []);

    const handleBackToCollections = useCallback(() => {
        setSelectedCollectionId(null);
        setSelectedCollectionName(null);
        setSelectedCollectionDescription(null);
        setCollectionArts([]);
    }, []);

    const handleCollectionClick = useCallback(async (collection) => {
        setSelectedCollectionName(collection.title || 'Без названия');
        setSelectedCollectionDescription(collection.description || '');
        setSelectedCollectionId(collection.id);
        await loadCollectionArts(collection);
    }, [loadCollectionArts]);

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
    const displayName = user.displayName || user.username;

    return (
        <>
            {/* === HEADER === */}
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
                        <span className={styles.displayName}>{displayName}</span>
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
            
            {/* === SWITCHER TABS === */}
            <div className={styles.menu}>
                <div className={styles.switcher}> 
                    <button 
                        className={`${styles.switcherBtn} ${activeTab === 'arts' ? styles.active : ''}`}
                        onClick={() => handleTabChange('arts')}
                    >
                        Арты
                    </button>
                    <span className={styles.separator}>/</span>
                    <button 
                        className={`${styles.switcherBtn} ${activeTab === 'collections' ? styles.active : ''}`}
                        onClick={() => handleTabChange('collections')}
                    >
                        Коллекции
                    </button>
                    {activeTab === 'collections' && selectedCollectionName && (
                        <div className={styles.collectionTitle}>
                            <span>{selectedCollectionName}</span>
                        </div>    
                    )}                    
                </div>
                {/* === НЕТ MenuOptions для чужого профиля === */}
            </div>

            {/* === COLLECTION DESCRIPTION === */}
            {activeTab === 'collections' && selectedCollectionName && (
                <div className={styles.description}>
                    <p className={styles.collectionDescription}>
                        {selectedCollectionDescription}
                    </p>
                </div>
            )}

            {/* === CONTENT FEED === */}
            <div className={styles.feedLayout}>
                {activeTab === 'collections' ? (
                    selectedCollectionId ? (
                        // === View: Arts inside collection ===
                        <div className={styles.collectionArtsView}>                            
                            {isLoadingCollectionArts ? (
                                <div className={styles.loading}>
                                    <div className={styles.spinner}></div>
                                    <span>Загрузка артов...</span>
                                </div>
                            ) : collectionArts.length > 0 ? (
                                <div className={styles.feed}>
                                    {collectionArts.map(art => (
                                        <ArtCard 
                                            key={art.id} 
                                            id={art.id} 
                                            image={getImageUrl(art.image || art.imageUrl)}
                                            typeShow="full"
                                            title={art.title || 'Без названия'}
                                            author={art.author?.username || user.username}
                                            likesCount={art.likesCount || 0}
                                            initialIsPrivate={art.isPublicFlag === false}
                                        />
                                    ))}
                                </div>
                            ) : (
                                <div className={styles.emptyState}>
                                    <span>В этой коллекции нет публичных артов</span>
                                </div>
                            )}
                        </div>
                    ) : (
                        // === View: Collections list ===
                        <div className={styles.collectionsFeed}>
                            {userCollections.length > 0 ? (
                                userCollections.map(collection => (
                                    <CollectionCard
                                        key={collection.id}
                                        id={collection.id}
                                        title={collection.title}
                                        coverImageUrl={collection.coverImageUrl}
                                        artCount={collection.artCount}
                                        isPublic={collection.isPublic}
                                        isLikedCollection={isSystemCollection(collection)}
                                        username={collection.username}
                                        // === НЕТ кнопок удаления/приватности для чужого профиля ===
                                        showDeleteIcon={false}
                                        showPrivacyIcon={false}
                                        onClick={() => handleCollectionClick(collection)}
                                    />
                                ))
                            ) : (
                                <div className={styles.emptyState}>
                                    <span>Нет публичных коллекций</span>
                                </div>
                            )}
                        </div>
                    )
                ) : (
                    // === TAB: Arts ===
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
                )}
            </div>
        </>
    );
}