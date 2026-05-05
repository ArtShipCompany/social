import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { useApi } from '../../hooks/useApi';
import { userApi } from '../../api/userApi';
import { followApi } from '../../api/followApi';
import { artApi } from '../../api/artApi';
import { useNotification } from '../../contexts/NotificationContext';
import styles from './Me.module.css';
import PFP from '../../assets/WA.jpg';
import editIcon from '../../assets/edit-profile-icon.svg';
import createIcon from '../../assets/create-icon.svg';
import ProfileOptionsMenu from '../../components/ProfileOptionsMenu/ProfileOptionsMenu';
import ConfirmModal from '../../components/ConfirmModal/ConfirmModal';
import ArtCard from '../../components/ArtCard/ArtCard';
import ProfileStats from '../../components/ProfileStats/ProfileStats';

export default function Me() {
    const navigate = useNavigate();
    const { user: currentUser, isAuthenticated, setUser: updateAuthUser } = useAuth();
    const notification = useNotification();
    
    const [userArts, setUserArts] = useState([]);
    const [followerCount, setFollowerCount] = useState(0);
    const [followingCount, setFollowingCount] = useState(0);
    const [showDeleteIcons, setShowDeleteIcons] = useState(false);
    const [showPrivacyIcons, setShowPrivacyIcons] = useState(false);
    const [isMenuOpen, setIsMenuOpen] = useState(false);
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
            // Загрузка артов
            const artsData = await artApi.getMyArts(0, 20);
            const artsList = artsData?.content || artsData || [];
            setUserArts(Array.isArray(artsList) ? artsList : []);
            
            // Загрузка счётчиков подписок
            const counts = await followApi.getFollowCounts(currentUser.id);
            setFollowerCount(counts?.followers ?? 0);
            setFollowingCount(counts?.following ?? 0);
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

    useEffect(() => {
        const refreshUserData = async () => {
            if (!isAuthenticated || !currentUser?.id) return;
            
            try {
                // Перезагружаем текущего пользователя из API
                const freshUser = await userApi.getCurrentUser();
                if (freshUser) {
                    setUser(freshUser);
                    localStorage.setItem('user', JSON.stringify(freshUser));
                }
                
                // Перезагружаем арты
                await loadUserData();
            } catch (err) {
                console.error('[Me] Ошибка обновления:', err);
            }
        };
        
        refreshUserData();
    }, [isAuthenticated, currentUser?.id]);

    const closeMenuAndResetModes = useCallback(() => {
        setShowDeleteIcons(false);
        setShowPrivacyIcons(false);
        setIsMenuOpen(false);
    }, []);

    const toggleMenu = useCallback(() => {
        isMenuOpen ? closeMenuAndResetModes() : setIsMenuOpen(true);
    }, [isMenuOpen, closeMenuAndResetModes]);

    const handleCreateClick = useCallback(() => {
        setIsMenuOpen(false);
        navigate('/create');
    }, [navigate]);

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

                <Link to="/edit" className={styles.edit}>
                    <img src={editIcon} alt="edit" />
                    <span>Редактировать</span>
                </Link>

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
                        
                        <div className={styles.buttonsCover}>
                            <ProfileOptionsMenu 
                                isOpen={isMenuOpen}
                                onToggle={toggleMenu}
                                onDeleteClick={() => setShowDeleteIcons(prev => !prev)}
                                onPrivacyClick={() => setShowPrivacyIcons(prev => !prev)}
                                onCreateClick={handleCreateClick}
                            />
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