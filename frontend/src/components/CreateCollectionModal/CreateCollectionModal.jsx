import { useState, useRef, useEffect } from 'react';
import styles from './CreateCollectionModal.module.css';
import { collectionsApi, LIKED_COLLECTION_ID } from '../../api/collectionsApi';
import { useNotification } from '../../contexts/NotificationContext';
import DefaultBtn from '../DefaultBtn/DefaultBtn';
import PlusIcon from '../../assets/create.svg';
import CloseIcon from '../../assets/cross-delete.svg';

export default function CreateCollectionModal({ isOpen, onClose, onSuccess, collection }) {
    const notification = useNotification();
    const fileInputRef = useRef(null);
    
    const [coverImage, setCoverImage] = useState(null);
    const [coverImageFile, setCoverImageFile] = useState(null);
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [isPublic, setIsPublic] = useState(true);
    const [loading, setLoading] = useState(false);

    const isEditMode = !!collection;
    const MAX_DESC_LENGTH = 500;

    useEffect(() => {
        if (isOpen && isEditMode && collection) {
            setTitle(collection.title || '');
            setDescription(collection.description || '');
            setIsPublic(collection.isPublic !== false);
            setCoverImage(collection.coverImageUrl || null);
        }
    }, [isOpen, isEditMode, collection]);

    const handleImageChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            if (file.size > 10 * 1024 * 1024) {
                notification.error('Размер файла не должен превышать 10MB', 3000);
                return;
            }
            
            const reader = new FileReader();
            reader.onloadend = () => {
                setCoverImage(reader.result);
                setCoverImageFile(file);
            };
            reader.readAsDataURL(file);
        }
    };

    const handleDrop = (e) => {
        e.preventDefault();
        const file = e.dataTransfer.files[0];
        if (file && file.type.startsWith('image/')) {
            const input = { target: { files: [file] } };
            handleImageChange(input);
        }
    };

    const handleDragOver = (e) => {
        e.preventDefault();
    };

    const handleRemoveImage = () => {
        setCoverImage(null);
        setCoverImageFile(null);
        if (fileInputRef.current) {
            fileInputRef.current.value = '';
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!title.trim()) {
            notification.error('Введите название коллекции', 3000);
            return;
        }

        setLoading(true);

        try {
            if (isEditMode) {
                // === РЕДАКТИРОВАНИЕ ===
                const updatedData = {
                    id: collection.id,
                    title: title.trim(),
                    description: description.trim(),
                    isPublic,
                    coverImageFile,
                };
                
                await collectionsApi.updateCollection(collection.id, updatedData);
                notification.success('Коллекция обновлена!', 3000);
                
                if (onSuccess) {
                    onSuccess(updatedData);
                }
            } else {
                // === СОЗДАНИЕ ===
                await collectionsApi.createCollection({
                    title: title.trim(),
                    description: description.trim(),
                    isPublic,
                    coverImageFile,
                });
                notification.success('Коллекция создана!', 3000);
                
                if (onSuccess) {
                    onSuccess();
                }
            }
            
            handleClose();
        } catch (error) {
            console.error(`Ошибка ${isEditMode ? 'обновления' : 'создания'} коллекции:`, error);
            notification.error(error.message || `Не удалось ${isEditMode ? 'обновить' : 'создать'} коллекцию`, 3000);
        } finally {
            setLoading(false);
        }
    };

    const handleClose = () => {
        setCoverImage(null);
        setCoverImageFile(null);
        setTitle('');
        setDescription('');
        setIsPublic(true);
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className={styles.overlay}>
            <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
                <button className={styles.closeBtn} onClick={handleClose}>
                    <img src={CloseIcon} alt="close" />
                </button>

                <h2 className={styles.title}>
                    {isEditMode ? 'Редактировать коллекцию' : 'Создать коллекцию'}
                </h2>

                <form className={styles.form} onSubmit={handleSubmit}>
                    {/* Загрузка обложки */}
                    <div className={styles.coverSection}>
                        <label>Обложка:</label>
                        
                        {!coverImage ? (
                            <div 
                                className={styles.uploadZone}
                                onDrop={handleDrop}
                                onDragOver={handleDragOver}
                                onClick={() => fileInputRef.current?.click()}
                            >
                                <input
                                    ref={fileInputRef}
                                    type="file"
                                    accept="image/*"
                                    onChange={handleImageChange}
                                    className={styles.fileInput}
                                    disabled={loading}
                                />
                                <img src={PlusIcon} alt="plus" className={styles.plusIcon} />
                                <span>Нажмите для загрузки изображения</span>
                            </div>
                        ) : (
                            <div className={styles.imagePreview}>
                                <img src={coverImage} alt="preview" className={styles.previewImg} />
                                <button 
                                    type="button"
                                    className={styles.removeBtn}
                                    onClick={handleRemoveImage}
                                    disabled={loading}
                                >
                                    Удалить
                                </button>
                            </div>
                        )}
                    </div>

                    {/* Приватность */}
                    <div className={styles.inputGroup}>
                        <label className={styles.privacyLabel}>Приватность:</label>
                        <div className={styles.toggleLabel}>
                            <span className={styles.toggleHint}>
                                {isPublic 
                                    ? 'Коллекция будет видна всем в ленте и поиске' 
                                    : 'Коллекция будет видна только вам'}
                            </span>
                            <div className={styles.toggleSwitch}>
                                <button
                                    type="button"
                                    className={`${styles.toggleOption} ${isPublic ? styles.active : ''}`}
                                    onClick={() => setIsPublic(true)}
                                    disabled={loading}
                                >
                                    Публичный
                                </button>
                                <button
                                    type="button"
                                    className={`${styles.toggleOption} ${!isPublic ? styles.active : ''}`}
                                    onClick={() => setIsPublic(false)}
                                    disabled={loading}
                                >
                                    Приватный
                                </button>
                            </div>
                        </div>
                    </div>

                    {/* Название */}
                    <div className={styles.inputGroup}>
                        <label htmlFor="collectionTitle">Название:</label>
                        <input
                            type="text"
                            id="collectionTitle"
                            value={title}
                            onChange={(e) => {
                                if (e.target.value.length <= 100) {
                                    setTitle(e.target.value);
                                }
                            }}
                            placeholder="Введите название коллекции"
                            disabled={loading}
                            maxLength={100}
                        />
                    </div>

                    {/* Описание */}
                    <div className={styles.inputGroup}>
                        <label htmlFor="collectionDescription">Описание:</label>
                        <div className={styles.textareaWrapper}>
                            <textarea
                                id="collectionDescription"
                                value={description}
                                onChange={(e) => {
                                    if (e.target.value.length <= MAX_DESC_LENGTH) {
                                        setDescription(e.target.value);
                                    }
                                }}
                                placeholder="Расскажите о вашей коллекции..."
                                className={styles.descriptionTextarea}
                                disabled={loading}
                                maxLength={MAX_DESC_LENGTH}
                                rows={4}
                            />
                            <div className={styles.charCount}>
                                {description.length}/{MAX_DESC_LENGTH}
                            </div>
                        </div>
                    </div>

                    {/* Кнопки */}
                    <div className={styles.buttons}>
                        <DefaultBtn 
                            type="button" 
                            text="Отмена" 
                            onClick={handleClose} 
                            disabled={loading}
                            className={styles.cancelBtn}
                        />
                        <DefaultBtn 
                            type="submit" 
                            text={loading 
                                ? (isEditMode ? 'Обновление...' : 'Создание...') 
                                : (isEditMode ? 'Обновить' : 'Создать')} 
                            disabled={loading}
                        />
                    </div>
                </form>
            </div>
        </div>
    );
}