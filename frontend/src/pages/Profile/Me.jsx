import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { userApi } from '../../api/userApi';
import { followApi } from '../../api/followApi';
import { artApi } from '../../api/artApi';
import { useNotification } from '../../contexts/NotificationContext';
import styles from './Me.module.css';
import PFP from '../../assets/WA.jpg';
import editIcon from '../../assets/edit-profile-icon.svg';
import createIcon from '../../assets/create-icon.svg'
import artsIcon from '../../assets/arts-icon.svg';
import ProfileOptionsMenu from '../../components/ProfileOptionsMenu/ProfileOptionsMenu';
import ConfirmModal from '../../components/ConfirmModal/ConfirmModal';
import ArtCard from '../../components/ArtCard/ArtCard';

export default function Me() {
    const navigate = useNavigate();
    const { user: currentUser, isAuthenticated, logout } = useAuth();
    const notification = useNotification();
    
    const [userArts, setUserArts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [followerCount, setFollowerCount] = useState(0);
    const [followingCount, setFollowingCount] = useState(0);
    const [showDeleteIcons, setShowDeleteIcons] = useState(false);
    const [showPrivacyIcons, setShowPrivacyIcons] = useState(false);
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [showConfirmModal, setShowConfirmModal] = useState(false);
    const [modalArtId, setModalArtId] = useState(null);
    const [deletingArtId, setDeletingArtId] = useState(null);

    const getImageUrl = useCallback((imagePath) => {
        return userApi.getFullUrl(imagePath) || '/default-art.jpg';
    }, []);

    const isValidArt = useCallback((art) => {
        return art && art.id && (art.image || art.imageUrl) && (art.image !== 'string');
    }, []);

    // Загрузка данных пользователя
    const loadUserData = useCallback(async () => {
        if (!isAuthenticated || !currentUser) {
            return;
        }

        try {
            setLoading(true);
            setError(null);
            
            // GET ARTS BY AUTHOR
            try {
                const artsData = await artApi.getMyArts();
                
                let formattedArts = [];
                
                if (artsData && artsData.content && Array.isArray(artsData.content)) {
                    formattedArts = artsData.content;
                } 
                
                setUserArts(formattedArts || []);
                
            } catch (artsError) {
                console.error('Ошибка загрузки артов:', artsError);
                setUserArts([]);
            }
            
            try {
                const [followers, following] = await Promise.all([
                    followApi.getFollowerCount(currentUser.id).catch(() => 0),
                    followApi.getFollowingCount(currentUser.id).catch(() => 0)
                ]);
                
                setFollowerCount(followers);
                setFollowingCount(following);
            } catch (statsError) {
                console.error('Ошибка загрузки статистики:', statsError);
            }
            
        } catch (err) {
            console.error('Ошибка загрузки профиля:', err);
            setError(err.message || 'Не удалось загрузить данные профиля');
        } finally {
            setLoading(false);
        }
    }, [currentUser, isAuthenticated]);

    useEffect(() => {
        if (!isAuthenticated || !currentUser) {
            const timer = setTimeout(() => {
                console.log('Перенаправление на /login из Me');
                navigate('/login');
            }, 100);
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
            
            await artApi.deleteArt(modalArtId);
            
            setUserArts(prevArts => prevArts.filter(art => art.id !== modalArtId));
            
            setShowConfirmModal(false);
            setModalArtId(null);

            notification.success('Арт успешно удалён!', 3000);
            
            if (userArts.length <= 1) {
                setShowDeleteIcons(false);
            }
            
        } catch (error) {
            console.error('Ошибка удаления арта:', error);
            notification.error(error.message, 3000)
        } finally {
            setDeletingArtId(null);
        }
    }, [modalArtId, userArts.length]);

    const cancelDelete = useCallback(() => {
        setShowConfirmModal(false);
        setModalArtId(null);
    }, []);

    // Функция для изменения приватности арта
    const toggleArtPrivacy = useCallback(async (artId) => {
        try {
            const artToUpdate = userArts.find(art => art.id === artId);
            if (!artToUpdate) return;
            
            const newIsPublic = !(artToUpdate.isPublicFlag === true);
            
            await artApi.updateArtPrivacy(artId, newIsPublic, currentUser.id);
            
            setUserArts(prevArts => 
                prevArts.map(art => 
                    art.id === artId 
                        ? { ...art, isPublicFlag: newIsPublic }
                        : art
                )
            );

            notification.info(`Арт теперь ${newIsPublic ? 'публичный' : 'приватный'}`, 3000);
            
        } catch (error) {
            console.error('Ошибка изменения приватности:', error);
            notification.error(`Ошибка изменения приватности: ${error.message}`, 3000);
        }
    }, [userArts]);

    if (!isAuthenticated) {
        return (
            <div className={styles.loading}>
                <div className={styles.spinner}></div>
                <span>Перенаправление на страницу входа...</span>
            </div>
        );
    }

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

    const validArts = userArts.filter(isValidArt);
    console.log(validArts)
    const displayNameToShow = currentUser.displayName || currentUser.username;

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