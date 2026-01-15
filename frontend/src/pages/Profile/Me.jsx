import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { userApi } from '../../api/userApi';
import { followApi } from '../../api/followApi';
import { artApi } from '../../api/artApi';
import styles from './Me.module.css';
import PFP from '../../assets/WA.jpg';
import editIcon from '../../assets/edit-profile-icon.svg';
import artsIcon from '../../assets/arts-icon.svg';
import ProfileOptionsMenu from '../../components/ProfileOptionsMenu/ProfileOptionsMenu';
import ConfirmModal from '../../components/ConfirmModal/ConfirmModal';
import ArtCard from '../../components/ArtCard/ArtCard';

export default function Me() {
    const navigate = useNavigate();
    const { user: currentUser, isAuthenticated, logout } = useAuth();
    
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
            return; // Не загружаем данные, если пользователь не авторизован
        }

        try {
            setLoading(true);
            setError(null);
            
            // Загружаем арты текущего пользователя
            try {
                const artsData = await artApi.getArtsByAuthor(currentUser.id);
                
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
    }, [currentUser, isAuthenticated]); // Убрали navigate из зависимостей

    useEffect(() => {
        // Проверяем аутентификацию перед загрузкой данных
        if (!isAuthenticated || !currentUser) {
            // Используем setTimeout для предотвращения конфликта с AuthContext
            const timer = setTimeout(() => {
                console.log('Перенаправление на /login из Me');
                navigate('/login');
            }, 100);
            return () => clearTimeout(timer);
        }
        
        // Если пользователь авторизован, загружаем данные
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

    // Функция для создания арта
    const handleCreateClick = useCallback(() => {
        setIsMenuOpen(false);
        navigate('/create-art');
    }, [navigate]);

    const openConfirmModal = useCallback((id) => {
        setModalArtId(id);
        setShowConfirmModal(true);
    }, []);

    const confirmDelete = useCallback(async () => {
        if (!modalArtId) return;
        
        try {
            setDeletingArtId(modalArtId);
            
            // Удаляем арт из базы данных
            await artApi.deleteArt(modalArtId);
            
            // Обновляем состояние локально
            setUserArts(prevArts => prevArts.filter(art => art.id !== modalArtId));
            
            // Закрываем модальное окно
            setShowConfirmModal(false);
            setModalArtId(null);
            
            // Показываем уведомление об успешном удалении
            alert(`Арт успешно удалён!`);
            
            // Если остался только один арт и он удаляется, сбрасываем режим удаления
            if (userArts.length <= 1) {
                setShowDeleteIcons(false);
            }
            
        } catch (error) {
            console.error('Ошибка удаления арта:', error);
            alert(`Ошибка удаления арта: ${error.message}`);
        } finally {
            setDeletingArtId(null);
        }
    }, [modalArtId, userArts.length]);

    const cancelDelete = useCallback(() => {
        setShowConfirmModal(false);
        setModalArtId(null);
    }, []);

    const handleLogout = useCallback(async () => {
        try {
            await logout();
            // navigate не нужен здесь, так как AuthContext уже перенаправит
        } catch (error) {
            console.error('Ошибка выхода:', error);
        }
    }, [logout]);

    // Функция для изменения приватности арта
    const toggleArtPrivacy = useCallback(async (artId) => {
        try {
            // Находим арт в массиве
            const artToUpdate = userArts.find(art => art.id === artId);
            if (!artToUpdate) return;
            
            // Определяем новое значение приватности
            const newIsPublic = !(artToUpdate.isPublic === true);
            
            // Подготавливаем данные для обновления
            const updateData = {
                isPublic: newIsPublic
            };
            
            // Обновляем арт в базе данных
            const updatedArt = await artApi.updateArt(artId, updateData);
            
            // Обновляем состояние локально
            setUserArts(prevArts => 
                prevArts.map(art => 
                    art.id === artId 
                        ? { ...art, ...updatedArt }
                        : art
                )
            );
            
            // Показываем уведомление
            alert(`Арт теперь ${newIsPublic ? 'публичный' : 'приватный'}`);
            
        } catch (error) {
            console.error('Ошибка изменения приватности:', error);
            alert(`Ошибка изменения приватности: ${error.message}`);
        }
    }, [userArts]);

    // Если проверка аутентификации еще в процессе, показываем загрузку
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
                    <div className={styles.nameContainer}>
                        <span className={styles.displayName}>@{displayNameToShow}</span>
                        <span className={styles.nickname}>({currentUser.username || 'user'})</span>
                    </div>
                </div>

                <div className={styles.contentWrapper}>
                    <div className={styles.headBg}></div>

                    <Link to="/edit" className={styles.edit}>
                        <img src={editIcon} alt="Редактировать профиль" />
                        <span>Редактировать</span>
                    </Link>

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
                                initialIsPrivate={art.isPublic === false}
                                likesCount={art.likesCount || 0}
                                isDeleting={deletingArtId === art.id}
                            />
                        );
                    })
                ) : (
                    <div className={styles.emptyState}>
                        <p>У вас пока нет артов. Создайте первый!</p>
                        <Link to="/create" className={styles.createArtButton}>
                            Создать арт
                        </Link>
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