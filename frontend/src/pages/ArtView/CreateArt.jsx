import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import styles from './CreateArt.module.css';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import CustomTextArea from '../../components/CustomTextArea/CustomTextArea';
import { artApi } from '../../api/artApi';
import { tagApi } from '../../api/tagApi';
import createIcon from '../../assets/create-icon.svg'

export default function CreateArt() {
    const navigate = useNavigate();
    const { user: currentUser, isAuthenticated } = useAuth();
    
    const MAX_LENGTH = 500;
    const MAX_TITLE_LENGTH = 100;
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [tags, setTags] = useState('');
    const [imageFile, setImageFile] = useState(null);
    const [imagePreview, setImagePreview] = useState(null);
    const [isPublic, setIsPublic] = useState(true);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [tagSuggestions, setTagSuggestions] = useState([]);
    const fileInputRef = useRef(null);

    // Проверка авторизации
    useEffect(() => {
        if (!isAuthenticated || !currentUser) {
            navigate('/login');
        }
    }, [isAuthenticated, currentUser, navigate]);

    // Автодополнение тегов
    useEffect(() => {
        const fetchSuggestions = async () => {
            if (!tags) return;
            
            const lastTag = tags.split(' ').pop();
            if (lastTag.startsWith('#') && lastTag.length > 1) {
                const query = lastTag.substring(1);
                try {
                    const suggestions = await tagApi.autocompleteTags(query);
                    setTagSuggestions(suggestions.slice(0, 5));
                } catch (error) {
                    console.error('Ошибка автодополнения тегов:', error);
                    setTagSuggestions([]);
                }
            } else {
                setTagSuggestions([]);
            }
        };
        
        const timeoutId = setTimeout(fetchSuggestions, 300);
        return () => clearTimeout(timeoutId);
    }, [tags]);

    const handleImageChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            // Проверяем тип файла
            if (!file.type.startsWith('image/')) {
                setError('Пожалуйста, выберите изображение');
                return;
            }
            
            // Проверяем размер файла (макс 10MB)
            if (file.size > 10 * 1024 * 1024) {
                setError('Размер файла не должен превышать 10MB');
                return;
            }
            
            setImageFile(file);
            setError(null);
            
            // Создаем превью
            const reader = new FileReader();
            reader.onloadend = () => {
                setImagePreview(reader.result);
            };
            reader.onerror = () => {
                setError('Ошибка при чтении файла');
                setImagePreview(null);
            };
            reader.readAsDataURL(file);
        }
    };

    const handleUploadClick = () => {
        fileInputRef.current?.click();
    };

    const handleRemoveImage = () => {
        setImageFile(null);
        setImagePreview(null);
        if (fileInputRef.current) {
            fileInputRef.current.value = '';
        }
    };

    const handleTitleChange = (e) => {
        const value = e.target.value;
        if (value.length <= MAX_TITLE_LENGTH) {
            setTitle(value);
        }
    };

    const handleTagsChange = (e) => {
        const value = e.target.value;
        if (value.length <= MAX_LENGTH) {
            setTags(value);
        }
    };

    const handleDescriptionChange = (e) => {
        const value = e.target.value;
        if (value.length <= MAX_LENGTH) {
            setDescription(value);
        }
    };

    const handleAddSuggestion = (tagName) => {
        const tagsArray = tags.split(' ').filter(t => t.trim());
        tagsArray.pop(); 
        tagsArray.push(`#${tagName}`);
        setTags(tagsArray.join(' ') + ' ');
        setTagSuggestions([]);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);

        // Валидация
        if (!title.trim()) {
            setError('Пожалуйста, введите заголовок арта');
            return;
        }

        if (!imageFile) {
            setError('Пожалуйста, выберите изображение');
            return;
        }

        try {
            setLoading(true);

            // Подготавливаем данные
            const artData = {
                title: title.trim(),
                description: description.trim(),
                isPublic: isPublic
            };

            console.log('Создаю арт с данными:', artData);

            // Создаем арт
            const createdArt = await artApi.createArt(artData, imageFile);
            console.log('Арт создан:', createdArt);

            // Если есть теги, обрабатываем их
            if (tags.trim()) {
                try {
                    const tagNames = tagApi.parseTagsString(tags);
                    console.log('Обрабатываю теги:', tagNames);
                    
                    if (tagNames.length > 0) {
                        // Создаем теги и связываем с артом
                        for (const tagName of tagNames) {
                            try {
                                const tag = await tagApi.getOrCreateTag(tagName);
                                await tagApi.addTagToArt(createdArt.id, tag.id);
                                console.log(`Тег "${tagName}" добавлен`);
                            } catch (tagError) {
                                console.error(`Ошибка добавления тега "${tagName}":`, tagError);
                            }
                        }
                    }
                } catch (tagError) {
                    console.error('Ошибка обработки тегов:', tagError);
                    // Не прерываем выполнение, просто показываем предупреждение
                }
            }

            // Перенаправляем на страницу созданного арта
            navigate(`/art/${createdArt.id}`);

        } catch (error) {
            console.error('Ошибка создания арта:', error);
            setError(error.message || 'Не удалось создать арт. Попробуйте снова.');
        } finally {
            setLoading(false);
        }
    };

    const handleCancel = () => {
        navigate(-1); // Возвращаемся назад
    };

    if (!isAuthenticated || !currentUser) {
        return (
            <div className={styles.loading}>
                <div className={styles.spinner}></div>
                <span>Перенаправление...</span>
            </div>
        );
    }

    return (
        <div className={styles.createArtContainer}>
            <div className={styles.createArtWrapper}>
                <h2 className={styles.title}>Создать новый арт</h2>
                
                <form onSubmit={handleSubmit} className={styles.form}>
                    {/* Загрузка изображения */}
                    <div className={styles.imageUploadSection}>
                        <label className={styles.sectionLabel}>Изображение*</label>
                        
                        <div className={styles.uploadArea}>
                            {imagePreview ? (
                                <div className={styles.imagePreviewContainer}>
                                    <div className={styles.imagePreviewWrapper}>
                                        <img 
                                            src={imagePreview} 
                                            alt="Предпросмотр" 
                                            className={styles.imagePreview}
                                            onError={(e) => {
                                                e.target.src = '/default-art.jpg';
                                            }}
                                        />
                                        <button 
                                            type="button"
                                            className={styles.removeImageButton}
                                            onClick={handleRemoveImage}
                                            title="Удалить изображение"
                                        >
                                            ×
                                        </button>
                                    </div>
                                    <p className={styles.imageInfo}>
                                        {imageFile?.name} ({(imageFile?.size / 1024 / 1024).toFixed(2)} MB)
                                    </p>
                                </div>
                            ) : (
                                <div 
                                    className={styles.uploadPlaceholder}
                                    onClick={handleUploadClick}
                                >
                                    <img src={createIcon} alt="Загрузить" className={styles.uploadIcon} />
                                    <p className={styles.uploadText}>Нажмите для загрузки изображения</p>
                                    <p className={styles.uploadHint}>Поддерживаемые форматы: JPG, PNG, GIF</p>
                                    <p className={styles.uploadHint}>Максимальный размер: 10MB</p>
                                </div>
                            )}
                            
                            <input
                                ref={fileInputRef}
                                type="file"
                                accept="image/*"
                                onChange={handleImageChange}
                                style={{ display: 'none' }}
                                disabled={loading}
                            />
                        </div>
                    </div>

                    {/* Основная информация */}
                    <div className={styles.formSection}>
                        <div className={styles.formGroup}>
                            <label htmlFor="title" className={styles.formLabel}>
                                Заголовок* <span className={styles.charCount}>{title.length}/{MAX_TITLE_LENGTH}</span>
                            </label>
                            <input
                                id="title"
                                type="text"
                                value={title}
                                onChange={handleTitleChange}
                                maxLength={MAX_TITLE_LENGTH}
                                placeholder="Введите заголовок арта"
                                className={styles.titleInput}
                                disabled={loading}
                                required
                            />
                        </div>

                        <div className={styles.formGroup}>
                            <label htmlFor="tags" className={styles.formLabel}>
                                Теги <span className={styles.charCount}>{tags.length}/{MAX_LENGTH}</span>
                            </label>
                            <CustomTextArea
                                value={tags}
                                onChange={handleTagsChange}
                                maxLength={MAX_LENGTH}
                                placeholder="например: #живопись #art #fyp"
                                id="tags"
                                disabled={loading}
                                rows={3}
                            />
                            
                            {/* Автодополнение тегов */}
                            {tagSuggestions.length > 0 && (
                                <div className={styles.tagSuggestions}>
                                    <span className={styles.suggestionsLabel}>Предложения:</span>
                                    <div className={styles.suggestionsList}>
                                        {tagSuggestions.map(tag => (
                                            <button
                                                key={tag.id}
                                                type="button"
                                                className={styles.suggestionTag}
                                                onClick={() => handleAddSuggestion(tag.name)}
                                                disabled={loading}
                                            >
                                                #{tag.name}
                                            </button>
                                        ))}
                                    </div>
                                </div>
                            )}
                        </div>

                        <div className={styles.formGroup}>
                            <label htmlFor="description" className={styles.formLabel}>
                                Описание <span className={styles.charCount}>{description.length}/{MAX_LENGTH}</span>
                            </label>
                            <CustomTextArea
                                value={description}
                                onChange={handleDescriptionChange}
                                maxLength={MAX_LENGTH}
                                placeholder="Расскажите о вашем арте..."
                                id="description"
                                disabled={loading}
                                rows={6}
                            />
                        </div>

                        <div className={styles.privacySettings}>
                            <label className={styles.privacyLabel}>
                                <input
                                    type="checkbox"
                                    checked={isPublic}
                                    onChange={(e) => setIsPublic(e.target.checked)}
                                    disabled={loading}
                                    className={styles.privacyCheckbox}
                                />
                                <span className={styles.privacyText}>
                                    Сделать публичным
                                </span>
                            </label>
                            <p className={styles.privacyHint}>
                                {isPublic 
                                    ? 'Арт будет виден всем пользователям'
                                    : 'Арт будет виден только вам'
                                }
                            </p>
                        </div>
                    </div>

                    {/* Сообщения об ошибках */}
                    {error && (
                        <div className={styles.errorMessage}>
                            {error}
                        </div>
                    )}

                    {/* Кнопки действий */}
                    <div className={styles.buttonsSection}>
                        <DefaultBtn 
                            type="button"
                            text="Отмена" 
                            className={styles.cancelButton}
                            onClick={handleCancel}
                            disabled={loading}
                        />
                        
                        <DefaultBtn 
                            type="submit"
                            text={loading ? "Создание..." : "Создать арт"} 
                            className={styles.submitButton}
                            disabled={loading}
                        />
                    </div>

                    <div className={styles.helpText}>
                        <p>* - обязательные поля</p>
                        <p>После создания арта вы сможете отредактировать его в любое время</p>
                    </div>
                </form>
            </div>
        </div>
    );
}