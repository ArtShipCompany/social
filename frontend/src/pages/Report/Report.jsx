import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { useNotification } from '../../contexts/NotificationContext';
import { artApi } from '../../api/artApi';
import { reportsApi } from '../../api/reportsApi';
import CustomTextArea from '../../components/CustomTextArea/CustomTextArea';
import styles from './Report.module.css';

const REPORT_REASONS = [
    { value: 'SPAM', label: 'Спам', description: 'Реклама, повторяющиеся сообщения или ссылки' },
    { value: 'HARASSMENT', label: 'Домогательства/Оскорбления', description: 'Оскорбительное или угрожающее поведение' },
    { value: 'COPYRIGHT', label: 'Нарушение авторских прав', description: 'Использование чужого контента без разрешения' },
    { value: 'VIOLENCE', label: 'Насилие/Жестокость', description: 'Сцены насилия или жестокости' },
    { value: 'ADULT', label: 'Взрослый контент', description: 'Контент 18+, неприемлемый для общего доступа' },
    { value: 'HATE_SPEECH', label: 'Разжигание ненависти', description: 'Дискриминация по расовому, религиозному или иному признаку' },
    { value: 'OTHER', label: 'Другое', description: 'Другая причина, не указанная выше' }
];

function Report() {
    const { artId } = useParams();
    const navigate = useNavigate();
    const { user } = useAuth();
    const notification = useNotification();
    
    const [art, setArt] = useState(null);
    const [loading, setLoading] = useState(true);
    const [selectedReason, setSelectedReason] = useState('');
    const [description, setDescription] = useState('');
    const [submitting, setSubmitting] = useState(false);
    
    const textareaRef = useRef(null);
    
    useEffect(() => {
        if (!user) {
            navigate('/login', { state: { from: `/report/art/${artId}` } });
        }
    }, [user, navigate, artId]);
    
    // Загрузка информации об арте
    useEffect(() => {
        const loadArt = async () => {
            try {
                setLoading(true);
                const artData = await artApi.getArtById(artId);
                setArt(artData);
            } catch (error) {
                console.error('Error loading art:', error);
                notification.error('Арт не найден');
                navigate(-1);
            } finally {
                setLoading(false);
            }
        };
        
        if (artId && user) {
            loadArt();
        }
    }, [artId, navigate, notification, user]);
    
    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!selectedReason) {
            notification.error('Выберите причину жалобы');
            return;
        }
        
        setSubmitting(true);
        try {
            await reportsApi.createArtReport(artId, selectedReason, description);
            notification.success('Жалоба отправлена. Спасибо за помощь в модерации!');
            navigate(`/art/${artId}`);
        } catch (error) {
            if (error.message?.includes('already reported')) {
                notification.error('Вы уже отправляли жалобу на этот арт');
            } else {
                notification.error('Ошибка при отправке жалобы');
            }
        } finally {
            setSubmitting(false);
        }
    };
    
    if (loading) {
        return (
            <div className={styles.loading}>
                <div className={styles.spinner}></div>
                <p>Загрузка...</p>
            </div>
        );
    }
    
    if (!art) {
        return (
            <div className={styles.error}>
                <h2>Арт не найден</h2>
                <button onClick={() => navigate(-1)}>Вернуться назад</button>
            </div>
        );
    }
    
    return (
        <div className={styles.container}>  
            <div className={styles.reportCard}>
                <h1 className={styles.title}>Пожаловаться на арт</h1>
                <p className={styles.subtitle}>
                    Ваша жалоба поможет нам сделать сообщество лучше. 
                    Модераторы рассмотрят её в ближайшее время.
                </p>
                
                {/* Информация об арте */}
                <div className={styles.artInfo}>
                    <img 
                        src={art.imageUrl} 
                        alt={art.title}
                        className={styles.artImage}
                    />
                    <div className={styles.artDetails}>
                        <h3>{art.title}</h3>
                        <p>Автор: {art.author?.displayName || art.author?.username}</p>
                        <p>Создан: {new Date(art.createdAt).toLocaleDateString('ru-RU')}</p>
                    </div>
                </div>
                
                {/* Форма жалобы */}
                <form onSubmit={handleSubmit} className={styles.form}>
                    <div className={styles.formGroup}>
                        <label>Причина жалобы *</label>
                        <div className={styles.reasonsList}>
                            {REPORT_REASONS.map(reason => (
                                <label key={reason.value} className={styles.reasonOption}>
                                    <input
                                        type="radio"
                                        name="reason"
                                        value={reason.value}
                                        checked={selectedReason === reason.value}
                                        onChange={(e) => setSelectedReason(e.target.value)}
                                    />
                                    <div className={styles.reasonContent}>
                                        <strong>{reason.label}</strong>
                                        <span className={styles.reasonDesc}>{reason.description}</span>
                                    </div>
                                </label>
                            ))}
                        </div>
                    </div>
                    
                    <div className={styles.formGroup}>
                        <CustomTextArea
                            ref={textareaRef}
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            maxLength={500}
                            placeholder="Опишите подробнее, что именно нарушает правила..."
                            label="Дополнительное описание"
                            id="description"
                        />
                    </div>
                    
                    <div className={styles.buttons}>
                        <button 
                            type="button" 
                            onClick={() => navigate(-1)} 
                            className={styles.cancelBtn}
                        >
                            Отмена
                        </button>
                        <button 
                            type="submit" 
                            disabled={submitting || !selectedReason}
                            className={styles.submitBtn}
                        >
                            {submitting ? 'Отправка...' : 'Отправить жалобу'}
                        </button>
                    </div>
                </form>
            </div>
        </div>

    );
}

export default Report;