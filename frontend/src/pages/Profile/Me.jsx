import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { useApi } from '../../hooks/useApi';
import { userApi } from '../../api/userApi';
import { followApi } from '../../api/followApi';
import { artApi } from '../../api/artApi';
import { linksApi } from '../../api/linksApi';
import { collectionsApi, LIKED_COLLECTION_ID } from '../../api/collectionsApi';

import { useNotification } from '../../contexts/NotificationContext';

import SocialLinks from '../../components/SocialLinks/SocialLinks';
import CreateCollectionModal from '../../components/CreateCollectionModal/CreateCollectionModal';

import styles from './Me.module.css';
import PFP from '../../assets/WA.jpg';
import editIcon from '../../assets/edit-icon.svg';
import createIcon from '../../assets/create-icon.svg';
import privacyIcon from '../../assets/private-edit.svg';
import deleteIcon from '../../assets/delete-icon.svg';
import settingsIcon from '../../assets/settings.svg';
import logoutIcon from '../../assets/logout.svg';
import ProfileOptionsMenu from '../../components/ProfileOptionsMenu/ProfileOptionsMenu';
import ConfirmModal from '../../components/ConfirmModal/ConfirmModal';
import ArtCard from '../../components/ArtCard/ArtCard';
import CollectionCard from '../../components/CollectionCard/CollectionCard';
import ProfileStats from '../../components/ProfileStats/ProfileStats';

export default function Me() {
    const navigate = useNavigate();
    const { user: currentUser, isAuthenticated, logout } = useAuth();
    const notification = useNotification();

    const [socialLinks, setSocialLinks] = useState([]);
    const [userArts, setUserArts] = useState([]);
    const [followerCount, setFollowerCount] = useState(0);
    const [followingCount, setFollowingCount] = useState(0);

    const [showDeleteIcons, setShowDeleteIcons] = useState(false);
    const [showPrivacyIcons, setShowPrivacyIcons] = useState(false);
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [isHeadMenuOpen, setIsHeadMenuOpen] = useState(false);

    // === Confirm Modal State (универсальный) ===
    const [showConfirmModal, setShowConfirmModal] = useState(false);
    const [confirmAction, setConfirmAction] = useState(null); // { type: 'art'|'collection', id, onConfirm }

    const [deletingArtId, setDeletingArtId] = useState(null);
    const [deletingCollectionId, setDeletingCollectionId] = useState(null);
    const [isInitialLoad, setIsInitialLoad] = useState(true);

    const [userCollections, setUserCollections] = useState([]);
    const [showCreateCollectionModal, setShowCreateCollectionModal] = useState(false);

    const [activeTab, setActiveTab] = useState('arts');
    const [selectedCollectionName, setSelectedCollectionName] = useState(null);

    const artApiHook = useApi(artApi);
    const followApiHook = useApi(followApi);
    const collectionsApiHook = useApi(collectionsApi);

    const getImageUrl = useCallback((imagePath) => {
        return artApi.utils?.getImageUrl?.(imagePath) || userApi.getFullUrl(imagePath) || PFP;
    }, []);

    const isValidArt = useCallback((art) => {
        return art?.id && (art.image || art.imageUrl) && art.image !== 'string';
    }, []);

    const loadUserData = useCallback(async () => {
        if (!isAuthenticated || !currentUser?.id) return;

        try {
            const artsData = await artApi.getMyArts(0, 20);
            const artsList = artsData?.content || artsData || [];
            setUserArts(Array.isArray(artsList) ? artsList : []);
            
            const collectionsData = await collectionsApi.getUserCollections(
                currentUser.id, 
                { page: 0, size: 20, includeLiked: true }
            );
            setUserCollections(collectionsData?.content || []);
            
            const counts = await followApi.getFollowCounts(currentUser.id);
            setFollowerCount(counts?.followers ?? 0);
            setFollowingCount(counts?.following ?? 0);

            const linksData = await linksApi.getMyLinks(true);
            setSocialLinks(linksData.links || []);
            
        } catch (err) {
            console.error('[Me] Ошибка загрузки:', err);
            notification.error('Не удалось загрузить данные профиля');
        } finally {
            setIsInitialLoad(false);
        }
    }, [currentUser, isAuthenticated, notification]);

    useEffect(() => {
        return () => {
            artApiHook.cancel();
            followApiHook.cancel();
            collectionsApiHook.cancel();
        };
    }, [artApiHook, followApiHook, collectionsApiHook]);

    useEffect(() => {
        if (!isAuthenticated || !currentUser) {
            const timer = setTimeout(() => navigate('/login'), 100);
            return () => clearTimeout(timer);
        }
        loadUserData();
    }, [isAuthenticated, currentUser, navigate, loadUserData]);

    const closeMenuAndResetModes = useCallback(() => {
        setShowDeleteIcons(false);
        setShowPrivacyIcons(false);
        setIsMenuOpen(false);
    }, []);

    const toggleMenu = useCallback(() => {
        isMenuOpen ? closeMenuAndResetModes() : setIsMenuOpen(true);
    }, [isMenuOpen, closeMenuAndResetModes]);

    const toggleHeadMenu = useCallback(() => {
        setIsHeadMenuOpen(prev => !prev);
    }, []);

    const handleTabChange = useCallback((tab) => {
        setActiveTab(tab);
        if (tab === 'arts') setSelectedCollectionName(null);
    }, []);

    const handleCollectionCardClick = useCallback((collectionName) => {
        setSelectedCollectionName(collectionName);
    }, []);

    const handleCreateClick = useCallback(() => {
        setIsMenuOpen(false);
        navigate('/create');
    }, [navigate]);

    const handleSettingsClick = useCallback(() => {
        setIsMenuOpen(false);
        navigate('/settings');
    }, [navigate]);

    const handleLogoutClick = useCallback(async () => {
        try {
            await logout();
            navigate('/login');
        } catch (error) {
            notification.error('Ошибка при выходе', 3000);
        }
    }, [logout, navigate, notification]);

    // === УНИВЕРСАЛЬНЫЕ ХЕНДЛЕРЫ ПОДТВЕРЖДЕНИЯ ===
    
    const openConfirmModal = useCallback((type, id, onConfirm) => {
        setConfirmAction({ type, id, onConfirm });
        setShowConfirmModal(true);
    }, []);

    const closeConfirmModal = useCallback(() => {
        setShowConfirmModal(false);
        setConfirmAction(null);
    }, []);

    const handleConfirmDelete = useCallback(async () => {
        if (!confirmAction) return;
        
        const { type, id, onConfirm } = confirmAction;
        
        try {
            if (type === 'art') {
                setDeletingArtId(id);
            } else if (type === 'collection') {
                setDeletingCollectionId(id);
            }
            
            await onConfirm?.(id);
            
            if (type === 'art') {
                notification.success('Арт удалён', 3000);
            } else if (type === 'collection') {
                notification.success('Коллекция удалена', 3000);
            }
        } catch (error) {
            console.error('Ошибка удаления:', error);
            notification.error(error.message || 'Не удалось удалить', 3000);
        } finally {
            setDeletingArtId(null);
            setDeletingCollectionId(null);
            closeConfirmModal();
        }
    }, [confirmAction, closeConfirmModal, notification]);

    // === УДАЛЕНИЕ АРТА ===
    const handleDeleteArt = useCallback(async (artId) => {
        await artApi.deleteArt(artId);
        setUserArts(prev => prev.filter(art => art.id !== artId));
    }, []);

    const openConfirmDeleteArt = useCallback((artId) => {
        openConfirmModal('art', artId, handleDeleteArt);
    }, [openConfirmModal, handleDeleteArt]);

    // === УДАЛЕНИЕ КОЛЛЕКЦИИ ===
    const handleDeleteCollection = useCallback(async (collectionId) => {
        if (
            collectionId === LIKED_COLLECTION_ID ||
            collectionId === '__liked__' ||
            collectionId === 'liked'
        ) {
            throw new Error('Коллекцию "Мне понравилось" удалить нельзя');
        }
        await collectionsApi.deleteCollection(collectionId);
        setUserCollections(prev => prev.filter(c => c.id !== collectionId));
    }, []);

    const openConfirmDeleteCollection = useCallback((collectionId) => {
        openConfirmModal('collection', collectionId, handleDeleteCollection);
    }, [openConfirmModal, handleDeleteCollection]);

    // === ПРИВАТНОСТЬ ===
    const toggleArtPrivacy = useCallback(async (artId) => {
        const art = userArts.find(a => a.id === artId);
        if (!art) return;
        const newIsPublic = !(art.isPublicFlag === true);
        try {
            await artApi.updateArtPrivacy(artId, newIsPublic);
            setUserArts(prev => prev.map(a => 
                a.id === artId ? { ...a, isPublicFlag: newIsPublic } : a
            ));
            notification.info(`Арт теперь ${newIsPublic ? 'публичный' : 'приватный'}`, 3000);
        } catch (error) {
            notification.error('Не удалось изменить приватность', 3000);
        }
    }, [userArts, notification]);

    const toggleCollectionPrivacy = useCallback(async (collectionId) => {
        if (collectionId === collectionsApi.utils.LIKED_COLLECTION_ID) {
            notification.warning('Приватность системной коллекции нельзя изменить');
            return;
        }
        const collection = userCollections.find(c => c.id === collectionId);
        if (!collection) return;
        const newIsPublic = !collection.isPublic;
        try {
            await collectionsApi.updateCollection(collectionId, { isPublic: newIsPublic });
            setUserCollections(prev => prev.map(c => 
                c.id === collectionId ? { ...c, isPublic: newIsPublic } : c
            ));
            notification.info(`Коллекция теперь ${newIsPublic ? 'публичная' : 'приватная'}`, 3000);
        } catch (error) {
            notification.error('Не удалось изменить приватность', 3000);
        }
    }, [userCollections, notification]);

    const handleCollectionClick = useCallback((collectionId) => {
        if (
            collectionId === LIKED_COLLECTION_ID ||
            collectionId === '__liked__' ||
            collectionId === 'liked'
        ) {
            navigate('/collections/liked');
            return;
        }
        navigate(`/collections/${collectionId}`);
    }, [navigate]);

    const handleCollectionCreated = useCallback(() => {
        loadUserData();
    }, [loadUserData]);

    if (!isAuthenticated) {
        return <div className={styles.loading}>Перенаправление...</div>;
    }

    if (isInitialLoad || (artApiHook.loading && userArts.length === 0)) {
        return (
            <div className={styles.loading}>
                <div className={styles.spinner}></div>
                <span>Загрузка...</span>
            </div>
        );
    }

    if (artApiHook.error || followApiHook.error) {
        return (
            <div className={styles.error}>
                <p>Ошибка: {artApiHook.error?.message || followApiHook.error?.message}</p>
                <button onClick={() => navigate('/')} className={styles.backButton}>На главную</button>
            </div>
        );
    }

    const validArts = userArts.filter(isValidArt);
    const displayName = currentUser?.displayName || currentUser?.username;

    const headMenuOptions = [
        { key: 'settings', icon: settingsIcon, alt: 'Настройки', title: 'Настройки', onClick: handleSettingsClick },
        { key: 'logout', icon: logoutIcon, alt: 'Выйти', title: 'Выйти', onClick: handleLogoutClick }
    ];

    const artsMenuOptions = [
        {
            key: 'privacy',
            icon: privacyIcon,
            alt: 'Приватность',
            title: 'Изменить приватность',
            onClick: () => setShowPrivacyIcons(prev => !prev),
            closeOnClick: false
        },
        {
            key: 'delete',
            icon: deleteIcon,
            alt: 'Удалить',
            title: 'Удалить арт',
            onClick: () => setShowDeleteIcons(prev => !prev),
            closeOnClick: false
        },
        {
            key: 'create',
            icon: createIcon,
            alt: 'Создать',
            title: 'Создать арт',
            onClick: handleCreateClick,
            className: styles.createIcon,
            closeOnClick: false
        }
    ];

    const collectionsMenuOptions = [
        {
            key: 'privacy',
            icon: privacyIcon,
            alt: 'Приватность',
            title: 'Изменить приватность',
            onClick: () => setShowPrivacyIcons(prev => !prev),
            closeOnClick: false
        },
        {
            key: 'delete',
            icon: deleteIcon,
            alt: 'Удалить',
            title: 'Удалить коллекцию',
            onClick: () => setShowDeleteIcons(prev => !prev),
            closeOnClick: false
        },
        {
            key: 'create',
            icon: createIcon,
            alt: 'Создать',
            title: 'Создать коллекцию',
            onClick: () => {
                setIsMenuOpen(false);
                setShowCreateCollectionModal(true);
            },
            className: styles.createIcon,
            closeOnClick: false
        }
    ];

    return (
        <>
            <div className={styles.headContent}>
                <div className={styles.faceName}>
                    <img 
                        src={userApi.getFullUrl(currentUser?.avatarUrl) || PFP} 
                        alt="avatar" 
                        className={styles.pfp}
                        onError={(e) => { e.target.src = PFP; }}
                    />
                </div>

                <div className={styles.contentWrapper}>
                    <div className={styles.headBg}></div>
                    <div className={styles.nameContainer}>
                        <span className={styles.displayName}>{displayName}</span>
                        <span className={styles.nickname}>@{currentUser?.username}</span>
                    </div>

                    <div className={styles.headSFooter}>
                        <ProfileStats 
                            userId={currentUser?.id}
                            artsCount={validArts.length}
                            followersCount={followerCount}
                            followingCount={followingCount}
                        />
                        {currentUser?.bio && <div className={styles.bio}><span>{currentUser.bio}</span></div>}
                        <SocialLinks links={socialLinks} />
                        <div className={styles.buttonsCover}>
                            <ProfileOptionsMenu 
                                isOpen={isHeadMenuOpen}
                                onToggle={toggleHeadMenu}
                                options={headMenuOptions}
                            />
                        </div>  
                    </div>
                </div>
            </div>
            
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
                        <span className={styles.collectionTitle}>{selectedCollectionName}</span>
                    )}                    
                </div>
                
                <div className={styles.menuButtons}>
                    <ProfileOptionsMenu 
                        isOpen={isMenuOpen}
                        onToggle={toggleMenu}
                        options={activeTab === 'arts' ? artsMenuOptions : collectionsMenuOptions}
                    />
                </div>  
            </div>

            <div className={styles.feedLayout}>
                <div className={styles.feed}>
                    {activeTab === 'arts' ? (
                        validArts.length > 0 ? (
                            validArts.map(art => (
                                <ArtCard 
                                    key={art.id} 
                                    id={art.id} 
                                    image={getImageUrl(art.image || art.imageUrl)}
                                    typeShow="amount"
                                    showDeleteIcon={showDeleteIcons}
                                    showPrivacyIcon={showPrivacyIcons}
                                    onOpenConfirmModal={openConfirmDeleteArt}
                                    onTogglePrivacy={() => toggleArtPrivacy(art.id)}
                                    initialIsPrivate={art.isPublicFlag === false}
                                    likesCount={art.likesCount || 0}
                                    isDeleting={deletingArtId === art.id}
                                />
                            ))
                        ) : (
                            <div className={styles.emptyState}>
                                <span>Нет артов. Создайте первый!</span>
                                <button className={styles.createButton} onClick={handleCreateClick}>
                                    <img src={createIcon} alt="create" className={styles.icon} />
                                    Создать
                                </button>
                            </div>
                        )
                    ) : (
                        userCollections.length > 0 ? (
                            userCollections.map(collection => (
                                <CollectionCard
                                    key={collection.id}
                                    id={collection.id}
                                    title={collection.title}
                                    coverImageUrl={collection.coverImageUrl}
                                    artCount={collection.artCount}
                                    isPublic={collection.isPublic}
                                    isLikedCollection={collection.id === LIKED_COLLECTION_ID}
                                    username={collection.username}
                                    showDeleteIcon={showDeleteIcons && collection.id !== LIKED_COLLECTION_ID}
                                    showPrivacyIcon={showPrivacyIcons && collection.id !== LIKED_COLLECTION_ID}
                                    initialIsPrivate={!collection.isPublic}
                                    onDelete={() => openConfirmDeleteCollection(collection.id)}
                                    onTogglePrivacy={() => toggleCollectionPrivacy(collection.id)}
                                    onClick={handleCollectionClick}
                                />
                            ))
                        ) : (
                            <div className={styles.emptyState}>
                                <span>Нет коллекций. Создайте первую!</span>
                                <button className={styles.createButton} onClick={() => setShowCreateCollectionModal(true)}>
                                    <img src={createIcon} alt="create" className={styles.icon} />
                                    Создать коллекцию
                                </button>
                            </div>
                        )
                    )}
                </div>
            </div>

            {/* === УНИВЕРСАЛЬНЫЙ CONFIRM MODAL === */}
            <ConfirmModal
                isOpen={showConfirmModal}
                onClose={closeConfirmModal}
                onConfirm={handleConfirmDelete}
                title={confirmAction?.type === 'art' ? 'Удалить арт?' : 'Удалить коллекцию?'}
                message="Это действие нельзя отменить"
                confirmText="Удалить"
                cancelText="Отмена"
                isProcessing={deletingArtId !== null || deletingCollectionId !== null}
            />

            <CreateCollectionModal
                isOpen={showCreateCollectionModal}
                onClose={() => setShowCreateCollectionModal(false)}
                onSuccess={handleCollectionCreated}
            />
        </>
    );
}