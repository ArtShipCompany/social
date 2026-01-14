import { Link } from 'react-router-dom';
import { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import styles from './ArtPost.module.css';
import LikeBtn from '../LikeBtn/LikeBtn';
import DefaultBtn from '../DefaultBtn/DefaultBtn';
import CustomTextArea from '../CustomTextArea/CustomTextArea';
import editIcon from '../../assets/edit-profile-icon.svg';
import { artApi } from '../../api/artApi';
import { tagApi } from '../../api/tagApi';

export default function ArtPost({ 
    edited = false,
    artId,  
    image,
    owner, 
    description = '', 
    tags = '', 
}) {
    const { user } = useAuth();
    const MAX_LENGTH = 500;
    const [editDescription, setEditDescription] = useState(description);
    const [editTags, setEditTags] = useState(tags);
    const [isOwner, setIsOwner] = useState(false);
    const [artDetails, setArtDetails] = useState(null);
    const [loading, setLoading] = useState(!edited);
    const [imageLoaded, setImageLoaded] = useState(false);
    const [imageError, setImageError] = useState(false);
    const [saving, setSaving] = useState(false);
    const [tagSuggestions, setTagSuggestions] = useState([]);
    const hasLoadedRef = useRef(false);
    const imgRef = useRef(null);

    // Используем утилиты из artApi
    const { getImageUrl, preloadImage, handleImageError: apiHandleImageError } = artApi.utils;
    const [currentImageUrl, setCurrentImageUrl] = useState(getImageUrl(image));

    // Обновляем URL при изменении image пропса
    useEffect(() => {
        setCurrentImageUrl(getImageUrl(image));
        setImageLoaded(false);
        setImageError(false);
    }, [image, getImageUrl]);

    // Предзагрузка изображения
    useEffect(() => {
        if (!currentImageUrl || currentImageUrl === '/default-art.jpg') {
            setImageLoaded(true);
            return;
        }

        setImageLoaded(false);
        setImageError(false);
        
        preloadImage(currentImageUrl)
            .then((url) => {
                console.log('Image preloaded successfully:', url);
                setCurrentImageUrl(url);
                setImageLoaded(true);
            })
            .catch((error) => {
                console.error('Failed to preload image:', error);
                setImageError(true);
                setImageLoaded(true);
            });
    }, [currentImageUrl, preloadImage]);

    // Загрузка данных арта
    useEffect(() => {
        if (!artId || edited || hasLoadedRef.current) {
            setLoading(false);
            return;
        }
        
        const loadArtDetails = async () => {
            try {
                setLoading(true);
                hasLoadedRef.current = true;
                
                // Загружаем арт
                const data = await artApi.getArtById(artId);
                setArtDetails(data);
                
                // Загружаем теги арта
                try {
                    const tagsData = await tagApi.getTagsByArt(artId);
                    const tagsString = tagApi.formatTagsForDisplay(tagsData);
                    setEditTags(tagsString);
                } catch (tagError) {
                    console.error('Ошибка загрузки тегов:', tagError);
                }
                
                // Проверяем владельца
                if (user && data.author && data.author.id === user.id) {
                    setIsOwner(true);
                }
            } catch (error) {
                console.error('Ошибка загрузки арта:', error);
            } finally {
                setLoading(false);
            }
        };
        
        loadArtDetails();
        
        return () => {
            hasLoadedRef.current = false;
        };
    }, [artId, user, edited]);

    // Проверяем владельца на основе переданного owner
    useEffect(() => {
        if (owner && user) {
            setIsOwner(owner.id === user.id);
        }
    }, [owner, user]);

    // Автодополнение тегов
    useEffect(() => {
        const fetchSuggestions = async () => {
            if (!edited || !editTags) return;
            
            const lastTag = editTags.split(' ').pop();
            if (lastTag.startsWith('#') && lastTag.length > 1) {
                const query = lastTag.substring(1);
                try {
                    const suggestions = await tagApi.autocompleteTags(query);
                    setTagSuggestions(suggestions.slice(0, 5)); // Ограничиваем 5 предложениями
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
    }, [editTags, edited]);

    const handleTagsChange = (e) => {
        const value = e.target.value;
        if (value.length <= MAX_LENGTH) {
            setEditTags(value);
        }
    };

    const handleDescriptionChange = (e) => {
        const value = e.target.value;
        if (value.length <= MAX_LENGTH) {
            setEditDescription(value);
        }
    };

    const handleLikeChange = (newLikeCount) => {
        console.log(`Новое количество лайков: ${newLikeCount}`);
    };

    const handleAddSuggestion = (tagName) => {
        const tagsArray = editTags.split(' ').filter(t => t.trim());
        tagsArray.pop(); 
        tagsArray.push(`#${tagName}`);
        setEditTags(tagsArray.join(' ') + ' ');
        setTagSuggestions([]);
    };

    const handleSave = async () => {
        if (saving) return;
        
        setSaving(true);
        
        try {
            console.log('Начинаем сохранение арта:', artId);
            
            // 1. Обновляем основные данные арта
            const artData = {
                title: artDetails?.title || 'Без названия',
                description: editDescription,
                isPublic: artDetails?.isPublic !== false // сохраняем текущую настройку приватности
            };
            
            console.log('Данные для обновления арта:', artData);
            
            const updatedArt = await artApi.updateArt(artId, artData);
            console.log('Арт обновлен:', updatedArt);
            
            // 2. Обрабатываем теги (создаем и связываем с артом)
            if (editTags && editTags.trim()) {
                try {
                    console.log('Обрабатываем теги:', editTags);
                    
                    // Разбираем строку тегов на массив имен
                    const tagNames = tagApi.parseTagsString(editTags);
                    console.log('Извлеченные теги:', tagNames);
                    
                    if (tagNames.length > 0) {
                        // Удаляем старые теги
                        await tagApi.removeAllTagsFromArt(artId);
                        console.log('🗑️ Старые теги удалены');
                        
                        // Создаем новые теги и связываем с артом
                        for (const tagName of tagNames) {
                            try {
                                // Создаем или получаем тег
                                const tag = await tagApi.getOrCreateTag(tagName);
                                console.log(`Тег "${tagName}" создан/получен:`, tag.id);
                                
                                // Связываем тег с артом
                                await tagApi.addTagToArt(artId, tag.id);
                                console.log(`Тег "${tagName}" связан с артом`);
                            } catch (tagError) {
                                console.error(`Ошибка с тегом "${tagName}":`, tagError);
                                // Продолжаем с другими тегами
                            }
                        }
                        
                        console.log('🎉 Все теги обработаны');
                    }
                } catch (tagError) {
                    console.error('❌ Ошибка обработки тегов:', tagError);
                    // Не прерываем выполнение, показываем предупреждение
                    alert('Арт сохранен, но возникла проблема с тегами: ' + tagError.message);
                }
            } else {
                // Если строка тегов пустая, удаляем все теги
                await tagApi.removeAllTagsFromArt(artId);
                console.log('🗑️ Все теги удалены (пустая строка)');
            }
            
            console.log('Сохранение завершено');
            
            // Даем небольшую задержку для лучшего UX
            setTimeout(() => {
                window.location.href = `/art/${artId}`;
            }, 500);
            
        } catch (error) {
            console.error('Ошибка сохранения:', error);
            alert(`Ошибка сохранения: ${error.message}`);
        } finally {
            setSaving(false);
        }
    };

    const handleImageLoad = () => {
        setImageLoaded(true);
        setImageError(false);
        console.log('Image loaded successfully:', currentImageUrl);
    };

    const handleImageError = (e) => {
        const usedDefault = apiHandleImageError(e, currentImageUrl);
        if (usedDefault) {
            setImageError(true);
            setImageLoaded(true);
        }
    };

    if (loading && !edited) {
        return <div className={styles.loading}>Загрузка...</div>;
    }

    return(
        <div className={styles.artWrapp}>
            <div className={styles.artImage}>
                {!imageLoaded && !imageError && (
                    <div className={styles.imagePlaceholder}>
                        Загрузка изображения...
                    </div>
                )}
                
                {imageError && (
                    <div className={styles.imagePlaceholder}>
                        Не удалось загрузить изображение
                    </div>
                )}
                
                <img 
                    ref={imgRef}
                    src={currentImageUrl} 
                    alt="art" 
                    className={`${styles.art} ${imageLoaded ? styles.visible : styles.hidden}`}
                    onLoad={handleImageLoad}
                    onError={handleImageError}
                    loading="eager"
                    style={{ display: imageLoaded && !imageError ? 'block' : 'none' }}
                />
            </div>

            {!edited && (
                <div className={styles.content}>
                    <div className={styles.textAndLike}>
                        <div className={styles.authorSection}>
                            <Link to={isOwner ? "/me" : `/profile/${owner?.id || 'unknown'}`} className={styles.ownerLink}>
                                <div className={styles.authorInfo}>
                                    <img 
                                        src={owner?.pfp || '/default-avatar.png'} 
                                        alt={owner?.displayName || owner?.nickname || owner?.username || 'Автор'}
                                        className={styles.authorAvatar}
                                        onError={(e) => {
                                            e.target.src = '/default-avatar.png';
                                        }}
                                    />
                                    <div className={styles.authorDetails}>
                                        <span className={styles.authorName}>
                                            {owner?.displayName || owner?.nickname || owner?.username || 'Неизвестный автор'}
                                        </span>
                                    </div>
                                </div>
                            </Link>
                        </div>
                        
                        <LikeBtn 
                            className={styles.like} 
                            typeShow={"full"} 
                            artId={artId}
                            onLikeChange={handleLikeChange}
                        />
                    </div>
                    
                    <div className={styles.textContent}>
                        <div className={styles.tags}>
                            <span>{tags || '#no-tags'}</span>
                        </div>
                        <span className={styles.description}>
                            {description || 'Без описания'}
                        </span>
                    </div>
                </div>
            )}
            
            {edited && isOwner && (
                <div className={styles.editContent}>
                    <div className={styles.form}>
                        <CustomTextArea
                            value={editTags}
                            onChange={handleTagsChange}
                            maxLength={MAX_LENGTH}
                            placeholder="например: #живопись #art #fyp"
                            label="Тэги:"
                            id="editTags"
                            disabled={saving}
                        />
                        
                        {/* Автодополнение тегов */}
                        {tagSuggestions.length > 0 && (
                            <div className={styles.tagSuggestions}>
                                <span className={styles.suggestionsLabel}>Предложения:</span>
                                {tagSuggestions.map(tag => (
                                    <button
                                        key={tag.id}
                                        type="button"
                                        className={styles.suggestionTag}
                                        onClick={() => handleAddSuggestion(tag.name)}
                                        disabled={saving}
                                    >
                                        #{tag.name}
                                    </button>
                                ))}
                            </div>
                        )}

                        <CustomTextArea
                            value={editDescription}
                            onChange={handleDescriptionChange}
                            maxLength={MAX_LENGTH}
                            placeholder="Пара слов о вашем арте..."
                            label="Описание:"
                            id="editDescription"
                            disabled={saving}
                        />
                    </div>
                    <div className={styles.btnArea}>
                        <DefaultBtn 
                            text={saving ? "Сохранение..." : "Сохранить"} 
                            onClick={handleSave}
                            disabled={saving}
                        />
                        {saving && (
                            <div className={styles.savingNote}>
                                Сохранение может занять несколько секунд...
                            </div>
                        )}
                    </div>
                </div>
            )}

            {!edited && isOwner && (
                <Link to={`/art/${artId}/edit`} className={styles.edit}>
                    <img src={editIcon} alt="Редактировать" />
                    <span>Редактировать</span>
                </Link>
            )}

        </div>
    );
}