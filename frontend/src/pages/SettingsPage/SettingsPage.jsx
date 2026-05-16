import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { getAuthToken } from '../../api/authApi';
import { userApi } from '../../api/userApi';
import { linksApi } from '../../api/linksApi';
import styles from './SettingsPage.module.css'
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import PhotoIcon from '../../assets/edit-pfp.svg';
import deleteIcon from '../../assets/delete-icon.svg';
import blankPfp from '../../assets/blank-pfp.svg';
import SeeIcon from '../../assets/see-pass.svg';
import HideIcon from '../../assets/hide-pass.svg';
import EditIcon from '../../assets/edit-icon.svg'

export default function SettingsPage() {
    const MAX_LENGTH = 100;
    const navigate = useNavigate();
    const { user: currentUser, isAuthenticated, setUser } = useAuth();
    
    const [bio, setBio] = useState('');
    const [username, setUsername] = useState('');
    const [displayName, setDisplayName] = useState('');
    const [avatarUrl, setAvatarUrl] = useState(blankPfp);
    const [avatarFile, setAvatarFile] = useState(null);
    const [previewUrl, setPreviewUrl] = useState(null);

    // === LINKS STATES ===
    const [links, setLinks] = useState([]);
    const [linksLoading, setLinksLoading] = useState(false);
    const [linksError, setLinksError] = useState(null);
    const [linksSuccess, setLinksSuccess] = useState(false);

    // Форма добавления ссылки
    const [newLinkUrl, setNewLinkUrl] = useState('');
    const [newLinkVisible, setNewLinkVisible] = useState(true);

    // Форма редактирования ссылки
    const [editingLinkId, setEditingLinkId] = useState(null);
    const [editingLinkUrl, setEditingLinkUrl] = useState('');
    const [editingLinkVisible, setEditingLinkVisible] = useState(true);

    const [availablePlatforms, setAvailablePlatforms] = useState([]);

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);

    // Загрузка данных
    useEffect(() => {
        const token = getAuthToken();
        if (!token) {
            navigate('/login');
            return;
        }

        const loadUserData = async () => {
            try {
                const userData = await userApi.getCurrentUser();
                
                if (userData) {
                    setUsername(userData.username || '');
                    setDisplayName(userData.displayName || userData.username || '');
                    setBio(userData.bio || '');
                    setAvatarUrl(userData.avatarUrl ? userApi.getFullUrl(userData.avatarUrl) : blankPfp);
                }
            } catch (err) {
                console.error('Ошибка загрузки данных пользователя:', err);
                setError('Не удалось загрузить данные профиля');
            }
        };

        loadUserData();
    }, [navigate]);

    // Загрузка ссылок и платформ
    useEffect(() => {
        const token = getAuthToken();
        if (!token || !isAuthenticated) return;

        const loadLinksData = async () => {
            try {
                setLinksLoading(true);
                const [linksData, platforms] = await Promise.all([
                    linksApi.getMyLinks(false), // загружаем все, включая скрытые
                    linksApi.getPlatforms()
                ]);
                
                setLinks(linksApi.sortLinks(linksData.links));
                setAvailablePlatforms(platforms);
            } catch (err) {
                console.error('Ошибка загрузки ссылок:', err);
                setLinksError('Не удалось загрузить социальные ссылки');
            } finally {
                setLinksLoading(false);
            }
        };

        loadLinksData();
    }, [isAuthenticated]);

    // Очистка preview при размонтировании
    useEffect(() => {
        return () => {
            if (previewUrl) URL.revokeObjectURL(previewUrl);
        };
    }, [previewUrl]);

    const handleAvatarChange = (e) => {
        const file = e.target.files[0];
        if (!file) return;
        
        if (file.size > 5 * 1024 * 1024) {
            setError('Файл больше 5МБ');
            return;
        }
        if (!file.type.startsWith('image/')) {
            setError('Только изображения');
            return;
        }

        // Создаём preview
        if (previewUrl) URL.revokeObjectURL(previewUrl);
        const url = URL.createObjectURL(file);
        setPreviewUrl(url);
        setAvatarUrl(url);
        setAvatarFile(file);
        setError(null);
    };

    const updateUserInContext = useCallback((updatedUserData, newToken) => {
        if (!currentUser && !updatedUserData) return;
        
        const baseData = currentUser || {};
        
        const merged = {
            ...baseData,
            ...updatedUserData,
            username: updatedUserData.username || baseData.username
        };
        
        if (newToken) {
            localStorage.setItem('accessToken', newToken);
        }
        
        setUser(merged);
        localStorage.setItem('user', JSON.stringify(merged));
        
        console.log('User context updated:', merged);
    }, [currentUser, setUser]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        const token = getAuthToken();
        if (!token) {
            navigate('/login');
            return;
        }

        try {
            setLoading(true);
            setError(null);
            setSuccess(false);

            const updateData = {};
            
            if (username.trim() !== '') {
                updateData.username = username.trim();
            }
            if (displayName.trim() !== '') {
                updateData.displayName = displayName;
            }
            if (bio !== undefined) {
                updateData.bio = bio;
            }

            let result;

            if (avatarFile) {
                const formData = new FormData();
                Object.keys(updateData).forEach(key => {
                    formData.append(key, updateData[key]);
                });
                formData.append('avatarFile', avatarFile);
                
                for (let [key, value] of formData.entries()) {
                    console.log(`  ${key}:`, value instanceof File ? `File(${value.name})` : value);
                }
                
                result = await userApi.updateProfile({
                    ...updateData,
                    avatarFile,
                });
            } else if (Object.keys(updateData).length > 0) {
                result = await userApi.updateProfile(updateData);
            } else {
                navigate('/me');
                return;
            }

            // Обновляем контекст ПЕРЕД редиректом
            if (result?.user) {
                // Принудительно обновляем localStorage
                localStorage.setItem('user', JSON.stringify(result.user));
                
                // Обновляем контекст
                setUser(result.user);
                
                if (result.newToken) {
                    localStorage.setItem('accessToken', result.newToken);
                }
            }

            setSuccess(true);

            setTimeout(() => {
                navigate('/me');
            }, 500);

        } catch (err) {
            console.error('Ошибка обновления профиля:', err);
            setError(err.message || 'Не удалось обновить профиль');
        } finally {
            setLoading(false);
        }
    };

    // Сброс формы редактирования
    const resetLinkForm = () => {
        setEditingLinkId(null);
        setEditingLinkUrl('');
        setEditingLinkVisible(true);
    };

    // Обработчик начала редактирования
    const handleEditLink = (link) => {
        setEditingLinkId(link.id);
        setEditingLinkUrl(link.url);
        setEditingLinkVisible(link.visible);
    };

    // Обработчик удаления ссылки
    const handleDeleteLink = async (linkId) => {
        if (!window.confirm('Удалить эту ссылку?')) return;
        
        try {
            setLinksLoading(true);
            await linksApi.deleteLink(linkId);
            setLinks(prev => prev.filter(l => l.id !== linkId));
            setLinksSuccess(true);
            setTimeout(() => setLinksSuccess(false), 2000);
        } catch (err) {
            setLinksError(err.message || 'Ошибка удаления');
        } finally {
            setLinksLoading(false);
        }
    };

    // Обработчик сохранения (добавление или обновление)
    const handleSaveLink = async (e) => {
        e.preventDefault();
        
        const url = editingLinkId ? editingLinkUrl.trim() : newLinkUrl.trim();
        
        if (!url) {
            setLinksError('Введите корректный URL');
            return;
        }

        try {
            setLinksLoading(true);
            setLinksError(null);

            // Форматируем URL через хелпер из API
            const formattedUrl = linksApi.utils.formatFullUrl('TELEGRAM', url); // platform пока заглушка
            
            let result;
            if (editingLinkId) {
                result = await linksApi.updateLink(editingLinkId, 'TELEGRAM', formattedUrl, {
                    visible: links.find(l => l.id === editingLinkId)?.visible ?? true,
                    displayOrder: links.find(l => l.id === editingLinkId)?.displayOrder ?? 0
                });
                setLinks(prev => prev.map(l => l.id === editingLinkId ? result : l));
            } else {
                // Добавление новой — используем newLinkVisible
                const nextOrder = links.length > 0 
                    ? Math.max(...links.map(l => l.displayOrder ?? 0)) + 1 
                    : 0;
                result = await linksApi.addLink('TELEGRAM', formattedUrl, {
                    visible: newLinkVisible,
                    displayOrder: nextOrder
                });
                setLinks(prev => linksApi.sortLinks([...prev, result]));
            }
            
            setLinksSuccess(true);
            resetLinkForm();
            setNewLinkUrl(''); // Очищаем форму добавления
            setTimeout(() => setLinksSuccess(false), 2000);
            
        } catch (err) {
            console.error('Ошибка сохранения ссылки:', err);
            setLinksError(err.message || 'Не удалось сохранить ссылку');
        } finally {
            setLinksLoading(false);
        }
    };

    // Переключение видимости ссылки в списке
    const toggleLinkVisibility = async (link) => {
        try {
            const newVisible = !link.visible;
            // Оптимистичное обновление UI
            setLinks(prev => prev.map(l => l.id === link.id ? { ...l, visible: newVisible } : l));
            
            await linksApi.updateLink(link.id, link.platform, link.url, {
                visible: newVisible,
                displayOrder: link.displayOrder
            });
        } catch (err) {
            // Откат при ошибке
            setLinks(prev => prev.map(l => l.id === link.id ? { ...l, visible: link.visible } : l));
            setLinksError('Ошибка обновления видимости');
        }
    };

    if (!isAuthenticated || !currentUser) {
        return <div className={styles.loading}>Перенаправление...</div>;
    }

    return (
        <>
        <form className={styles.form} onSubmit={handleSubmit}>
            <h2 className={styles.title}>Редактировать профиль</h2>
            {/* Аватар */}
            <div className={styles.pfp}>
                <label htmlFor="avatarUpload" className={styles.avatarLabel}>
                    <img 
                        src={avatarUrl} 
                        alt="avatar" 
                        className={styles.avatarImg}
                        onError={(e) => { e.target.src = blankPfp; }}
                    />
                    <img src={PhotoIcon} alt="edit" className={styles.photoIcon}/>
                </label>
                <input
                    id="avatarUpload"
                    type="file"
                    accept="image/*"
                    onChange={handleAvatarChange}
                    style={{ display: 'none' }}
                    disabled={loading}
                />
            </div>
            <span className={styles.changePhotoText}>Изменить фото</span>

            {/* Поля */}
            <div className={styles.inputGroup}>
                <div className={styles.nameInput}>
                    <label>Никнейм:</label>
                    <div className={styles.usernameWrapper}>
                        <span className={styles.prefix}>@</span>
                        <input
                            type="text"
                            value={username}
                            onChange={(e) => {
                                const val = e.target.value.toLowerCase();
                                if (val.length <= 20) setUsername(val);
                            }}
                            placeholder="никнейм"
                            className={styles.usernameInput}
                            disabled={loading}
                        />
                    </div>
                </div>
                
                <div className={styles.nameInput}>
                    <label>Имя:</label>
                    <input
                        type="text"
                        value={displayName}
                        onChange={(e) => {
                            const val = e.target.value;
                            if (val.length <= MAX_LENGTH) setDisplayName(val);
                        }}
                        placeholder="Ваше имя"
                        disabled={loading}
                    />
                </div>
                
                <div className={styles.textareaWrapper}>
                    <label>О себе:</label>
                    <textarea
                        value={bio}
                        onChange={(e) => {
                            if (e.target.value.length <= MAX_LENGTH) setBio(e.target.value);
                        }}
                        maxLength={MAX_LENGTH}
                        placeholder="Расскажите о себе..."
                        className={styles.bioTextarea}
                        disabled={loading}
                    />
                    <div className={styles.charCount}>{bio.length}/{MAX_LENGTH}</div>
                </div>
            </div>

            {error && <div className={styles.errorMessage}>{error}</div>}

            <div className={styles.buttons}>
                <DefaultBtn type="button" text="Отмена" onClick={() => navigate('/me')} disabled={loading} />
                <DefaultBtn type="submit" text={loading ? 'Сохранение...' : 'Сохранить'} disabled={loading} />
            </div>
        </form>

        <div className={styles.linksSection}>
            <h2 className={styles.sectionTitle}>Социальные сети</h2>
            
            {/* Сообщения */}
            {linksError && <div className={styles.errorMessage}>{linksError}</div>}
            
            {/* Список существующих ссылок */}
            <div className={styles.linksList}>
                {linksLoading && links.length === 0 ? (
                    <div className={styles.loading}>Загрузка...</div>
                ) : links.length === 0 ? (
                    <span className={styles.emptyText}>Пока нет добавленных ссылок</span>
                ) : (
                    links.map((link, index) => {
                        const formatted = linksApi.utils.formatLink(link);
                        const isEditing = editingLinkId === link.id;
                        
                        return (
                            <div key={link.id} className={styles.linkItem}>
                                {isEditing ? (
                                    // === РЕЖИМ РЕДАКТИРОВАНИЯ ===
                                    <form onSubmit={handleSaveLink} className={styles.linkFormInline}>
                                        <input
                                            type="url"
                                            value={editingLinkUrl}
                                            onChange={(e) => setEditingLinkUrl(e.target.value)}
                                            placeholder="https://..."
                                            className={styles.linkInput}
                                            disabled={linksLoading}
                                            autoFocus
                                        />

                                        <div className={styles.linkActions}>
                                            <DefaultBtn 
                                                type="submit" 
                                                text="✓" 
                                                className={styles.smallBtn}
                                                disabled={linksLoading}
                                            />
                                            <DefaultBtn 
                                                type="button" 
                                                text="✕" 
                                                className={`${styles.smallBtn} ${styles.cancelBtn}`}
                                                onClick={resetLinkForm}
                                                disabled={linksLoading}
                                            />
                                        </div>
                                    </form>
                                ) : (
                                    // === РЕЖИМ ПРОСМОТРА ===
                                    <>
                                        <div className={styles.linkInfo}>
                                            <span className={styles.linkIcon}>{formatted?.icon}</span>
                                            <div className={styles.linkDetails}>
                                                <span className={styles.linkPlatform}>{formatted?.platformLabel}</span>
                                                <a 
                                                    href={formatted?.fullUrl} 
                                                    target="_blank" 
                                                    rel="noopener noreferrer"
                                                    className={`${styles.linkUrl} ${!link.visible ? styles.hidden : ''}`}
                                                >
                                                    {link.url.length > 30 ? link.url.slice(0, 30) + '...' : link.url}
                                                </a>
                                            </div>
                                        </div>
                                        
                                        <div className={styles.linkControls}>                                            
                                            {/* Видимость */}
                                            <button
                                                onClick={() => toggleLinkVisibility(link)}
                                                className={`${styles.smallBtn} ${!link.visible ? styles.inactive : ''}`}
                                                title={link.visible ? 'Скрыть' : 'Показать'}
                                                disabled={linksLoading}
                                            >
                                                <img
                                                    src={link.visible ? SeeIcon : HideIcon}
                                                    alt=""
                                                    className={styles.eyeIcon}
                                                />
                                            </button>
                                            
                                            {/* Редактировать */}
                                            <button
                                                onClick={() => handleEditLink(link)}
                                                className={styles.smallBtn}
                                                disabled={linksLoading}
                                                title="Редактировать"
                                            >
                                                <img src={EditIcon} alt="edit-icon" />
                                            </button>
                                            
                                            {/* Удалить */}
                                            <button
                                                onClick={() => handleDeleteLink(link.id)}
                                                className={styles.deleteBtn}
                                                disabled={linksLoading}
                                                title="Удалить"
                                            >
                                                <img src={deleteIcon} alt="delete-icon" />
                                            </button>                                            
                                        </div>
                                    </>
                                )}
                            </div>
                        );
                    })
                )}
            </div>

            <form onSubmit={handleSaveLink} className={styles.linkForm}>
                <input
                    type="url"
                    value={newLinkUrl}
                    onChange={(e) => setNewLinkUrl(e.target.value)}
                    placeholder="https://..."
                    className={styles.linkInput}
                    disabled={linksLoading || editingLinkId !== null}
                />
                
                <label className={styles.visibilityToggle}>
                    <input
                        type="checkbox"
                        checked={!newLinkVisible}
                        onChange={(e) => setNewLinkVisible(!e.target.checked)}
                        disabled={linksLoading || editingLinkId !== null}
                    />
                    <span className={styles.checkboxCustom}></span>
                    <span>Скрыть</span>
                </label>
                
                <DefaultBtn 
                    type="submit" 
                    text={linksLoading ? '...' : '+ Добавить'} 
                    disabled={linksLoading || editingLinkId !== null || !newLinkUrl.trim()}
                />
            </form>
        </div>

        <div className={styles.container}>
            <div className={styles.wrap}>
                <h2>Сбросить пароль</h2>
                <DefaultBtn
                    type="button"
                    text="Сбросить"
                    onClick={() => navigate('/forgot-password')}
                />
            </div>
        </div>

        <div className={styles.container}>
            <div className={styles.wrap}>
                <h2>Удалить аккаунт</h2>
                <DefaultBtn
                    type="button"
                    text="Удалить"
                    onClick={() => navigate('/')}
                />
            </div>
        </div>
        </>
    );
}