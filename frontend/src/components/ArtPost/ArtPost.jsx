import { Link, useNavigate } from 'react-router-dom';
import { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import styles from './ArtPost.module.css';
import LikeBtn from '../LikeBtn/LikeBtn';
import DefaultBtn from '../DefaultBtn/DefaultBtn';
import CustomTextArea from '../CustomTextArea/CustomTextArea';
import CreateIcon from '../../assets/create.svg';
import editIcon from '../../assets/edit-profile-icon.svg';
import { artApi } from '../../api/artApi';
import { tagApi } from '../../api/tagApi';

export default function ArtPost({ 
  mode = 'view', // 'view', 'edit', 'create'
  artId = '',  
  image = '',
  owner, 
  title = '',
  description = '', 
  tags = '', 
}) {
  const navigate = useNavigate();
  const { user } = useAuth();
  const MAX_LENGTH = 500;
  
  // Состояния для данных арта
  const [artTitle, setArtTitle] = useState(title);
  const [artDescription, setArtDescription] = useState(description);
  const [artTags, setArtTags] = useState(tags);
  const [artImage, setArtImage] = useState(image);
  
  // Состояния для загрузки и отображения
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [uploadedImage, setUploadedImage] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [imageLoaded, setImageLoaded] = useState(false);
  const [imageError, setImageError] = useState(false);
  const [isOwner, setIsOwner] = useState(false);
  const [artDetails, setArtDetails] = useState(null);
  
  // Для автодополнения тегов
  const [tagSuggestions, setTagSuggestions] = useState([]);
  
  const hasLoadedRef = useRef(false);
  const imgRef = useRef(null);
  
  // Получаем URL изображения
  const currentImageUrl = artApi.utils.getImageUrl(
    mode === 'create' ? imagePreview : 
    uploadedImage ? URL.createObjectURL(uploadedImage) : 
    artImage
  );

  // Инициализация состояний при изменении пропсов
  useEffect(() => {
    if (mode === 'edit' || mode === 'view') {
      setArtTitle(title || '');
      setArtDescription(description || '');
      setArtTags(tags || '');
      setArtImage(image || '');
    }
  }, [mode, title, description, tags, image]);

  // Загрузка данных арта для просмотра и редактирования
  useEffect(() => {
    if ((mode === 'view' || mode === 'edit') && artId && !hasLoadedRef.current) {
      loadArtDetails();
    }
    
    return () => {
      hasLoadedRef.current = false;
    };
  }, [mode, artId]);

  // Проверка владельца
  useEffect(() => {
    if (user && artDetails) {
      setIsOwner(artDetails.author?.id === user.id);
    } else if (user && owner) {
      setIsOwner(owner.id === user.id);
    }
  }, [user, artDetails, owner]);

  // Загрузка деталей арта
  const loadArtDetails = async () => {
    try {
      setLoading(true);
      hasLoadedRef.current = true;
      
      const data = await artApi.getArtById(artId);
      setArtDetails(data);
      
      // Устанавливаем данные
      setArtTitle(data.title || '');
      setArtDescription(data.description || '');
      setArtImage(data.imageUrl || '');
      
      // Загружаем теги
      try {
        const tagsData = await tagApi.getTagsByArt(artId);
        const tagsString = tagApi.formatTagsForDisplay(tagsData);
        setArtTags(tagsString);
      } catch (tagError) {
        console.error('Ошибка загрузки тегов:', tagError);
        setArtTags('');
      }
      
    } catch (error) {
      console.error('Ошибка загрузки арта:', error);
    } finally {
      setLoading(false);
    }
  };

  // Обработчик загрузки изображения
  const handleImageUpload = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    // Проверка типа файла
    if (!file.type.startsWith('image/')) {
      alert('Пожалуйста, выберите файл изображения');
      return;
    }

    // Проверка размера (макс 10MB)
    if (file.size > 10 * 1024 * 1024) {
      alert('Размер файла не должен превышать 10MB');
      return;
    }

    setUploadedImage(file);
    
    // Создаем превью
    const reader = new FileReader();
    reader.onloadend = () => {
      setImagePreview(reader.result);
    };
    reader.readAsDataURL(file);
  };

  // Обработчики изменения полей
  const handleTitleChange = (e) => {
    setArtTitle(e.target.value);
  };

  const handleTagsChange = (e) => {
    const value = e.target.value;
    if (value.length <= MAX_LENGTH) {
      setArtTags(value);
    }
  };

  const handleDescriptionChange = (e) => {
    const value = e.target.value;
    if (value.length <= MAX_LENGTH) {
      setArtDescription(value);
    }
  };

  // Автодополнение тегов
  useEffect(() => {
    const fetchSuggestions = async () => {
      if (mode !== 'edit' && mode !== 'create') return;
      if (!artTags || !artTags.trim()) {
        setTagSuggestions([]);
        return;
      }
      
      const lastTag = artTags.split(' ').pop();
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
  }, [artTags, mode]);

  const handleAddSuggestion = (tagName) => {
    const tagsArray = artTags.split(' ').filter(t => t.trim());
    tagsArray.pop();
    tagsArray.push(`#${tagName}`);
    setArtTags(tagsArray.join(' ') + ' ');
    setTagSuggestions([]);
  };

  // СОЗДАНИЕ НОВОГО АРТА
  const handleCreateArt = async () => {
    if (saving || !uploadedImage) return;
    
    if (!artTitle.trim()) {
      alert('Пожалуйста, введите заголовок арта');
      return;
    }

    setSaving(true);
    
    try {
      const artData = {
        title: artTitle.trim(),
        description: artDescription.trim(),
        isPublic: true // по умолчанию публичный
      };

      console.log('Создание арта:', artData);
      
      const createdArt = await artApi.createArt(artData, uploadedImage);
      console.log('Арт создан:', createdArt);
      
      // Обрабатываем теги если есть
      if (artTags && artTags.trim()) {
        try {
          const tagNames = tagApi.parseTagsString(artTags);
          if (tagNames.length > 0) {
            for (const tagName of tagNames) {
              try {
                const tag = await tagApi.getOrCreateTag(tagName);
                await tagApi.addTagToArt(createdArt.id, tag.id);
              } catch (tagError) {
                console.error(`Ошибка с тегом "${tagName}":`, tagError);
              }
            }
          }
        } catch (tagError) {
          console.error('Ошибка обработки тегов:', tagError);
        }
      }
      
      // Перенаправляем на страницу арта
      navigate(`/art/${createdArt.id}`);
      
    } catch (error) {
      console.error('Ошибка создания арта:', error);
      alert(`Ошибка создания арта: ${error.message}`);
    } finally {
      setSaving(false);
    }
  };

  // ОБНОВЛЕНИЕ АРТА
  const handleUpdateArt = async () => {
    if (saving || !artId) return;
    
    setSaving(true);
    
    try {
      const artData = {
        title: artTitle.trim(),
        description: artDescription.trim(),
        isPublic: artDetails?.isPublic !== false
      };

      console.log('Обновление арта:', artId, artData);
      
      // Обновляем арт с изображением если есть новое
      const updatedArt = await artApi.updateArt(
        artId, 
        artData, 
        uploadedImage || null
      );
      
      console.log('Арт обновлен:', updatedArt);
      
      // Обрабатываем теги
      try {
        // Удаляем старые теги
        await tagApi.removeAllTagsFromArt(artId);
        
        // Добавляем новые если есть
        if (artTags && artTags.trim()) {
          const tagNames = tagApi.parseTagsString(artTags);
          if (tagNames.length > 0) {
            for (const tagName of tagNames) {
              try {
                const tag = await tagApi.getOrCreateTag(tagName);
                await tagApi.addTagToArt(artId, tag.id);
              } catch (tagError) {
                console.error(`Ошибка с тегом "${tagName}":`, tagError);
              }
            }
          }
        }
      } catch (tagError) {
        console.error('Ошибка обработки тегов:', tagError);
        alert('Арт сохранен, но возникла проблема с тегами');
      }
      
      // Даем время для обновления и перенаправляем
      setTimeout(() => {
        navigate(`/art/${artId}`);
      }, 500);
      
    } catch (error) {
      console.error('Ошибка обновления арта:', error);
      alert(`Ошибка обновления: ${error.message}`);
    } finally {
      setSaving(false);
    }
  };

  // Обработчик сохранения (общий для create и edit)
  const handleSave = () => {
    if (mode === 'create') {
      handleCreateArt();
    } else if (mode === 'edit') {
      handleUpdateArt();
    }
  };

  // Обработчики для изображения
  const handleImageLoad = () => {
    setImageLoaded(true);
    setImageError(false);
  };

  const handleImageError = (e) => {
    const usedDefault = artApi.utils.handleImageError(e, currentImageUrl);
    if (usedDefault) {
      setImageError(true);
      setImageLoaded(true);
    }
  };

  if (loading && mode === 'view') {
    return <div className={styles.loading}>Загрузка арта...</div>;
  }

  return (
    <div className={styles.artWrapp}>
      {/* БЛОК ИЗОБРАЖЕНИЯ */}
      <div className={styles.artImage}>
        {mode === 'create' && (
          <>
            <input
              id="artUpload"
              type="file"
              accept="image/*"
              onChange={handleImageUpload}
              style={{ display: 'none' }}
            />
            
            {imagePreview ? (
              <div className={styles.artPreviewWrapper}>
                <img 
                  src={imagePreview} 
                  alt="Предпросмотр" 
                  className={styles.artPreview}
                />
                <label htmlFor="artUpload" className={styles.changePhotoBtn}>
                  Изменить фото
                </label>
              </div>
            ) : (
              <div className={styles.artAdd}>
                <label htmlFor="artUpload" className={styles.upload}>
                  <img src={CreateIcon} alt="create-icon" className={styles.addIcon}/>
                  <span>Нажмите для загрузки изображения</span>
                  <div className={styles.foot}>
                    <span>Поддерживаемые форматы: JPEG, PNG</span>
                    <span>Максимальный размер: 10MB</span>
                  </div>
                </label>
              </div>
            )}
          </>
        )}
        
        {(mode === 'view' || mode === 'edit') && (
          <>
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
          </>
        )}
      </div>

      {/* БЛОК РЕДАКТИРОВАНИЯ/СОЗДАНИЯ */}
      {(mode === 'create' || mode === 'edit') && (
        <div className={styles.editContent}>
          <div className={styles.form}>
            {/* Заголовок */}
            <label htmlFor="artTitle">Заголовок:</label>
            <div className={styles.inputWrapper}>
              <input 
                type="text" 
                id="artTitle"
                value={artTitle}
                onChange={handleTitleChange}
                placeholder='Введите заголовок арта'
                disabled={saving}
                maxLength={100}
              />
            </div>

            {/* Теги */}
            <CustomTextArea
              value={artTags}
              onChange={handleTagsChange}
              maxLength={MAX_LENGTH}
              placeholder="например: #живопись #art #fyp"
              label="Тэги:"
              id="artTags"
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

            {/* Описание */}
            <CustomTextArea
              value={artDescription}
              onChange={handleDescriptionChange}
              maxLength={MAX_LENGTH}
              placeholder="Расскажите о вашем арте..."
              label="Описание:"
              id="artDescription"
              disabled={saving}
            />
          </div>
          
          {/* Кнопки действий */}
          <div className={styles.btnArea}>
            <DefaultBtn 
              text={saving ? "Сохранение..." : "Сохранить"} 
              onClick={handleSave}
              disabled={saving || (mode === 'create' && !uploadedImage)}
            />
            {saving && (
              <div className={styles.savingNote}>
                Сохранение может занять несколько секунд...
              </div>
            )}
          </div>
        </div>
      )}

      {/* БЛОК ПРОСМОТРА */}
      {mode === 'view' && (
        <>
          <div className={styles.content}>
            <div className={styles.textAndLike}>
              <div className={styles.authorSection}>
                <Link 
                  to={isOwner ? "/me" : `/profile/${owner?.id || artDetails?.author?.id || 'unknown'}`} 
                  className={styles.ownerLink}
                >
                  <div className={styles.authorInfo}>
                    <img 
                      src={owner?.pfp || artDetails?.author?.pfp || '/default-avatar.png'} 
                      alt="Автор"
                      className={styles.authorAvatar}
                      onError={(e) => {
                        e.target.src = '/default-avatar.png';
                      }}
                    />
                    <div className={styles.authorDetails}>
                      <span className={styles.authorName}>
                        {owner?.displayName || artDetails?.author?.displayName || 'Неизвестный автор'}
                      </span>
                    </div>
                  </div>
                </Link>
              </div>
              
              <LikeBtn 
                className={styles.like} 
                typeShow={"full"} 
                artId={artId}
              />
            </div>
            
            <div className={styles.textContent}>
              <h1 className={styles.artTitle}>{artTitle}</h1>
              <div className={styles.tags}>
                <span>{artTags || '#no-tags'}</span>
              </div>
              <span className={styles.description}>
                {artDescription || 'Без описания'}
              </span>
            </div>
          </div>

          {/* Кнопка редактирования для владельца */}
          {isOwner && (
            <Link to={`/art/${artId}/edit`} className={styles.edit}>
              <img src={editIcon} alt="Редактировать" />
              <span>Редактировать</span>
            </Link>
          )}
        </>
      )}
    </div>
  );
}