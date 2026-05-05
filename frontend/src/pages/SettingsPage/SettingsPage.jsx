import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { getAuthToken } from '../../api/authApi';
import { userApi } from '../../api/userApi';
import styles from './SettingsPage.module.css'
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import PhotoIcon from '../../assets/edit-pfp.svg';
import blankPfp from '../../assets/blank-pfp.svg';

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

    if (!isAuthenticated || !currentUser) {
        return <div className={styles.loading}>Перенаправление...</div>;
    }

    return (
        <form className={styles.form} onSubmit={handleSubmit}>
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
    );
}