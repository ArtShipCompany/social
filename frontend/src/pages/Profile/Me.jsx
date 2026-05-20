import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { useApi } from '../../hooks/useApi';
import { userApi } from '../../api/userApi';
import { followApi } from '../../api/followApi';
import { artApi } from '../../api/artApi';
import { useNotification } from '../../contexts/NotificationContext';
import SocialLinks from '../../components/SocialLinks/SocialLinks';
import { linksApi } from '../../api/linksApi';

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
import ProfileStats from '../../components/ProfileStats/ProfileStats';

export default function Me() {
    const navigate = useNavigate();
    const { user: currentUser, isAuthenticated, logout } = useAuth();
    const notification = useNotification();

    // ссыли соц сети
    const [socialLinks, setSocialLinks] = useState([]);
    
    const [userArts, setUserArts] = useState([]);
    const [followerCount, setFollowerCount] = useState(0);
    const [followingCount, setFollowingCount] = useState(0);
    const [showDeleteIcons, setShowDeleteIcons] = useState(false);
    const [showPrivacyIcons, setShowPrivacyIcons] = useState(false);
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [isHeadMenuOpen, setIsHeadMenuOpen] = useState(false);
    const [showConfirmModal, setShowConfirmModal] = useState(false);
    const [modalArtId, setModalArtId] = useState(null);
    const [deletingArtId, setDeletingArtId] = useState(null);
    const [isInitialLoad, setIsInitialLoad] = useState(true);

    const artApiHook = useApi(artApi);
    const followApiHook = useApi(followApi);

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
        };
    }, [artApiHook, followApiHook]);

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

    const openConfirmModal = useCallback((id) => {
        setModalArtId(id);
        setShowConfirmModal(true);
    }, []);

    const confirmDelete = useCallback(async () => {
        if (!modalArtId) return;
        
        try {
            setDeletingArtId(modalArtId);
            await artApi.deleteArt(modalArtId);
            
            setUserArts(prev => prev.filter(art => art.id !== modalArtId));
            setShowConfirmModal(false);
            setModalArtId(null);
            notification.success('Арт удалён', 3000);
            
            if (userArts.length <= 1) setShowDeleteIcons(false);
        } catch (error) {
            console.error('Ошибка удаления:', error);
            notification.error(error.message || 'Не удалось удалить', 3000);
        } finally {
            setDeletingArtId(null);
        }
    }, [modalArtId, userArts.length, notification]);

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
            console.error('Ошибка приватности:', error);
            notification.error('Не удалось изменить приватность', 3000);
        }
    }, [userArts, notification]);

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

    // Конфигурация меню для шапки профиля
    const headMenuOptions = [
        {
            key: 'settings',
            icon: settingsIcon,
            alt: 'Настройки',
            title: 'Настройки',
            onClick: handleSettingsClick
        },
        {
            key: 'logout',
            icon: logoutIcon,
            alt: 'Выйти',
            title: 'Выйти',
            onClick: handleLogoutClick
        }
    ];

    // Конфигурация меню для управления артами
    const artsMenuOptions = [
        {
            key: 'privacy',
            icon: privacyIcon,
            alt: 'Приватность',
            title: 'Изменить приватность',
            onClick: () => setShowPrivacyIcons(prev => !prev)
        },
        {
            key: 'delete',
            icon: deleteIcon,
            alt: 'Удалить',
            title: 'Удалить арт',
            onClick: () => setShowDeleteIcons(prev => !prev)
        },
        {
            key: 'create',
            icon: createIcon,
            alt: 'Создать',
            title: 'Создать арт',
            onClick: handleCreateClick,
            className: styles.createIcon
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
                    <span>Арты / Коллекции</span>
                </div>
                
                <div className={styles.menuButtons}>
                    <ProfileOptionsMenu 
                        isOpen={isMenuOpen}
                        onToggle={toggleMenu}
                        options={artsMenuOptions}
                    />
                </div>  
            </div>

            <div className={styles.feed}>
                {validArts.length > 0 ? (
                    validArts.map(art => (
                        <ArtCard 
                            key={art.id} 
                            id={art.id} 
                            image={getImageUrl(art.image || art.imageUrl)}
                            typeShow="amount"
                            showDeleteIcon={showDeleteIcons}
                            showPrivacyIcon={showPrivacyIcons}
                            onOpenConfirmModal={openConfirmModal}
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
                )}
            </div>

            <ConfirmModal
                isOpen={showConfirmModal}
                onClose={() => { setShowConfirmModal(false); setModalArtId(null); }}
                onConfirm={confirmDelete}
                title="Удалить арт?"
                message="Это действие нельзя отменить"
                confirmText="Удалить"
                cancelText="Отмена"
                isProcessing={deletingArtId !== null}
            />
        </>
    );
}