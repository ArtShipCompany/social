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
import artsIcon from '../../assets/arts-icon.svg';
import ProfileOptionsMenu from '../../components/ProfileOptionsMenu/ProfileOptionsMenu';
import ConfirmModal from '../../components/ConfirmModal/ConfirmModal';
import ArtCard from '../../components/ArtCard/ArtCard';

export default function Me() {
    const navigate = useNavigate();
    const { user: currentUser, isAuthenticated } = useAuth();
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

    // 👇 Добавляем состояние для отслеживания первой загрузки
    const [isInitialLoad, setIsInitialLoad] = useState(true);

    const artApiHook = useApi(artApi);
    const followApiHook = useApi(followApi);

    const getImageUrl = useCallback((imagePath) => {
        return artApi.utils?.getImageUrl?.(imagePath) || userApi.getFullUrl(imagePath) || '/default-art.jpg';
    }, []);

    const isValidArt = useCallback((art) => {
        return art && art.id && (art.image || art.imageUrl) && (art.image !== 'string');
    }, []);


const loadUserData = useCallback(async () => {
    if (!isAuthenticated || !currentUser?.id) return;

    try {
        console.log('[Me] Запрос артов...');
        // 👇 Используем напрямую artApi и followApi (они стабильны)
        const artsData = await artApi.getMyArts(0, 20);
        const formattedArts = artsData?.content && Array.isArray(artsData.content) 
            ? artsData.content 
            : (Array.isArray(artsData) ? artsData : []);
        setUserArts(formattedArts);
        
        console.log('[Me] Запрос счётчиков...');
        const counts = await followApi.getFollowCounts(currentUser.id);
        setFollowerCount(counts?.followers ?? 0);
        setFollowingCount(counts?.following ?? 0);
    } catch (err) {
        console.error('[Me] Критическая ошибка:', err);
    } finally {
        setIsInitialLoad(false);
    }
}, [currentUser, isAuthenticated]);

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
        if (isMenuOpen) {
            closeMenuAndResetModes();
        } else {
            setIsMenuOpen(true);
        }
    }, [isMenuOpen, closeMenuAndResetModes]);

    const handlePrivacyClick = useCallback(() => {
        setShowPrivacyIcons(prev => !prev);
        setShowDeleteIcons(false);
    }, []);

    const handleDeleteClick = useCallback(() => {
        setShowDeleteIcons(prev => !prev);
        setShowPrivacyIcons(false);
    }, []);

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

            const { error } = await artApiHook.callApiMethod('deleteArt', modalArtId);
            
            if (error) throw error;
            
            setUserArts(prev => prev.filter(art => art.id !== modalArtId));
            setShowConfirmModal(false);
            setModalArtId(null);
            notification.success('Арт успешно удалён!', 3000);
            
            if (userArts.length <= 1) setShowDeleteIcons(false);
            
        } catch (error) {
            console.error('Ошибка удаления арта:', error);
            notification.error(error.message || 'Не удалось удалить арт', 3000);
        } finally {
            setDeletingArtId(null);
        }
    }, [modalArtId, userArts.length, artApiHook, notification]);

    const cancelDelete = useCallback(() => {
        setShowConfirmModal(false);
        setModalArtId(null);
    }, []);

    // Функция для изменения приватности арта
    const toggleArtPrivacy = useCallback(async (artId) => {
        const artToUpdate = userArts.find(art => art.id === artId);
        if (!artToUpdate) return;
        
        const newIsPublic = !(artToUpdate.isPublicFlag === true);
        
        const { error } = await artApiHook.callApiMethod(
            'updateArtPrivacy', 
            artId, 
            newIsPublic, 
            currentUser.id
        );
        
        if (error) {
            console.error('Ошибка изменения приватности:', error);
            notification.error(`Ошибка: ${error.message}`, 3000);
            return;
        }
        
        setUserArts(prev => prev.map(art => 
            art.id === artId ? { ...art, isPublicFlag: newIsPublic } : art
        ));
        notification.info(`Арт теперь ${newIsPublic ? 'публичный' : 'приватный'}`, 3000);
        
    }, [userArts, currentUser?.id, artApiHook, notification]);

    const isLoading = artApiHook.loading || followApiHook.loading;
    const apiError = artApiHook.error || followApiHook.error;

    if (!isAuthenticated) {
        return <div className={styles.loading}>Перенаправление...</div>;
    }

    if (isInitialLoad || (artApiHook.loading && userArts.length === 0)) {
        return (
            <div className={styles.loading}>
                <div className={styles.spinner}></div>
                <span>Загрузка профиля...</span>
            </div>
        );
    }

    // 👇 Показываем ошибку ТОЛЬКО если есть ошибка (независимо от артов)
    if (apiError) {
        return (
            <div className={styles.error}>
                <h2>Ошибка</h2>
                <p>{apiError.message || 'Не удалось загрузить данные'}</p>
                <button onClick={() => navigate('/')} className={styles.backButton}>
                    Вернуться на главную
                </button>
            </div>
        );
    }

    const validArts = userArts.filter(isValidArt);
    const displayNameToShow = currentUser?.displayName || currentUser?.username;

    return (
        <>
            <div className={styles.headContent}>
                <div className={styles.faceName}>
                    <img 
                        src={userApi.getFullUrl(currentUser.avatarUrl) || PFP} 
                        alt="profile-photo" 
                        className={styles.pfp}
                        onError={(e) => {
                            e.target.src = PFP;
                        }}
                    />
                </div>

                <Link to="/edit" className={styles.edit}>
                    <img src={editIcon} alt="Редактировать профиль" />
                    <span>Редактировать</span>
                </Link>

                <div className={styles.contentWrapper}>
                    <div className={styles.headBg}></div>

                    <div className={styles.nameContainer}>
                        <span className={styles.displayName}>{displayNameToShow}</span>
                        <span className={styles.nickname}>@{currentUser.username || 'user'}</span>
                    </div>

                    <div className={styles.headSFooter}>
                        <div className={styles.stats}>
                            <div className={styles.arts}>
                                <img src={artsIcon} alt="Арты" />
                                <span>{` ${validArts.length}`}</span>
                            </div>
                            <span>Подписчики: {followerCount}</span>
                            <span>Подписки: {followingCount}</span>
                        </div>

                        {currentUser.bio && (
                            <div className={styles.bio}>
                                <span>{currentUser.bio}</span>
                            </div>
                        )}

                        <div className={styles.buttonsCover}>
                            <ProfileOptionsMenu 
                                isOpen={isMenuOpen}
                                onToggle={toggleMenu}
                                onPrivacyClick={handlePrivacyClick}
                                onDeleteClick={handleDeleteClick}
                                onCreateClick={handleCreateClick}
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
                                    typeShow="amount"
                                    showDeleteIcon={showDeleteIcons}
                                    showPrivacyIcon={showPrivacyIcons}
                                    onOpenConfirmModal={openConfirmModal}
                                    onTogglePrivacy={() => toggleArtPrivacy(art.id)}
                                    initialIsPrivate={art.isPublicFlag === false}
                                    likesCount={art.likesCount || 0}
                                    isDeleting={deletingArtId === art.id}
                                />
                            );
                        })
                ) : (
                    <div className={styles.emptyState}>
                        <span>У вас пока нет артов. Создайте первый!</span>
                        <button
                            className={styles.createButton}
                            onClick={handleCreateClick} 
                        >
                            <img src={createIcon} alt="Добавить" className={`${styles.icon} ${styles.createIcon}`} />
                            Создать
                        </button>
                    </div>
                )}
            </div>


            <ConfirmModal
                isOpen={showConfirmModal}
                onClose={cancelDelete}
                onConfirm={confirmDelete}
                title="Удаление арта"
                message={`Вы точно хотите удалить этот арт? Это действие невозможно отменить.`}
                confirmText="Удалить"
                cancelText="Отмена"
                isProcessing={deletingArtId !== null}
            />
        </>
    );
}