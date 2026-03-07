import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { userApi } from '../../api/userApi';
import styles from './Edit.module.css';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import PhotoIcon from '../../assets/edit-pfp.svg';
import blankPfp from '../../assets/blank-pfp.svg';

export default function Edit() {
    const MAX_LENGTH = 100;
    const navigate = useNavigate();
    const { user: currentUser, isAuthenticated } = useAuth();
    
    const [bio, setBio] = useState('');
    const [displayName, setDisplayName] = useState('');
    const [avatarUrl, setAvatarUrl] = useState(blankPfp);
    const [avatarFile, setAvatarFile] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);

    // Загрузка данных текущего пользователя
    useEffect(() => {
        const loadUserData = async () => {
            if (!isAuthenticated || !currentUser) {
                navigate('/login');
                return;
            }

            try {
                // Загружаем полные данные пользователя
                const userData = await userApi.getCurrentUser();
                
                if (userData) {
                    setDisplayName(userData.displayName || userData.username || '');
                    setBio(userData.bio || '');
                    setAvatarUrl(userApi.getAvatarUrl(userData) || blankPfp);
                }
            } catch (err) {
                console.error('Ошибка загрузки данных пользователя:', err);
                setError('Не удалось загрузить данные профиля');
            }
        };

        loadUserData();
    }, [currentUser, isAuthenticated, navigate]);

    const handleAvatarChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            // Проверяем размер файла (макс 5MB)
            if (file.size > 5 * 1024 * 1024) {
                setError('Файл слишком большой. Максимальный размер: 5MB');
                return;
            }
            
            // Проверяем тип файла
            if (!file.type.startsWith('image/')) {
                setError('Пожалуйста, выберите изображение');
                return;
            }

            const reader = new FileReader();
            reader.onloadend = () => {
                setAvatarUrl(reader.result);
                setAvatarFile(file);
                setError(null);
            };
            reader.onerror = () => {
                setError('Ошибка при чтении файла');
            };
            reader.readAsDataURL(file);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!isAuthenticated || !currentUser) {
            navigate('/login');
            return;
        }

        try {
            setLoading(true);
            setError(null);
            setSuccess(false);

            const updateData = {};
            
            // Добавляем поля только если они изменились
            if (displayName !== currentUser.displayName) {
                updateData.displayName = displayName;
            }
            
            if (bio !== currentUser.bio) {
                updateData.bio = bio;
            }

            if (avatarFile) {
                // Если есть новый аватар, используем FormData
                const formData = userApi.createProfileFormData({
                    ...updateData,
                    avatarFile: avatarFile
                });
                
                await userApi.updateProfileWithAvatar(formData);
            } else if (Object.keys(updateData).length > 0) {
                // Если изменены только текстовые поля
                await userApi.updateProfile(updateData);
            } else {
                // Ничего не изменилось
                navigate('/me');
                return;
            }

            // Вместо updateUser просто обновляем localStorage и перенаправляем
            if (updateData.displayName || updateData.bio || avatarFile) {
                // Обновляем данные в localStorage
                const storedUser = localStorage.getItem('user');
                if (storedUser) {
                    const parsedUser = JSON.parse(storedUser);
                    localStorage.setItem('user', JSON.stringify({
                        ...parsedUser,
                        ...updateData
                    }));
                }
            }

            setSuccess(true);
            
            // Перенаправляем через 2 секунды
            setTimeout(() => {
                // Принудительно обновляем страницу, чтобы получить свежие данные
                window.location.href = '/me';
            }, 2000);

        } catch (err) {
            console.error('Ошибка обновления профиля:', err);
            setError(err.message || 'Не удалось обновить профиль. Попробуйте снова.');
        } finally {
            setLoading(false);
        }
    };

    const handleCancel = () => {
        navigate('/me');
    };

    if (!isAuthenticated || !currentUser) {
        return (
            <div className={styles.loading}>
                <div className={styles.spinner}></div>
                <span>Перенаправление на страницу входа...</span>
            </div>
        );
    }

    return (
        <>
            <form className={styles.form} onSubmit={handleSubmit}>
                {/* загрузка фото */}
                <div className={styles.pfp}>
                    <label htmlFor="avatarUpload" className={styles.avatarLabel}>
                        <img 
                            src={avatarUrl} 
                            alt="profile-photo" 
                            className={styles.avatarImg} 
                            onError={(e) => {
                                e.target.src = blankPfp;
                            }}
                        />
                        <img src={PhotoIcon} alt="edit-photo" className={styles.photoIcon}/>
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

                <div className={styles.inputGroup}>
                    <div className={styles.nameInput}>
                        <label htmlFor="displayName">Имя</label>
                        <div className={styles.usernameWrapper}>
                            <span className={styles.prefix}>@</span>
                            <input
                                id="displayName"
                                type="text"
                                value={displayName}
                                onChange={(e) => {
                                    let value = e.target.value;
                                    if (value.length <= MAX_LENGTH) {
                                        setDisplayName(value);
                                    }
                                }}
                                placeholder="Ваше имя"
                                className={styles.usernameInput}
                                disabled={loading}
                            />
                        </div>
                    </div>
                    
                    <div className={styles.textareaWrapper}>
                        <label htmlFor="bio">Описание профиля</label>
                        <textarea
                            id="bio"
                            value={bio}
                            onChange={(e) => {
                                if (e.target.value.length <= MAX_LENGTH) {
                                    setBio(e.target.value);
                                }
                            }}
                            maxLength={MAX_LENGTH}
                            placeholder="Расскажите о себе..."
                            className={styles.bioTextarea}
                            disabled={loading}
                        />
                        <div className={styles.charCount}>
                            {bio.length}/{MAX_LENGTH}
                        </div>
                    </div>
                </div>

                {/* Сообщения об ошибках и успехе */}
                {error && (
                    <div className={styles.errorMessage}>
                        {error}
                    </div>
                )}
                
                {success && (
                    <div className={styles.successMessage}>
                        Профиль успешно обновлен! Перенаправление...
                    </div>
                )}

                {/* Кнопки действий */}
                <div className={styles.buttons}>
                    <DefaultBtn 
                        type="button"
                        text="Отмена" 
                        className={styles.cancelBtn}
                        onClick={handleCancel}
                        disabled={loading}
                    />
                    
                    <DefaultBtn 
                        type="submit"
                        text={loading ? "Сохранение..." : "Сохранить"} 
                        className={styles.saveBtn} 
                        disabled={loading}
                    />
                </div>

            </form>
        </>
    );
}