import { useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import styles from './ArtView.module.css';
import UpIcon from '../../assets/up-icon.svg';
import ArtPost from '../../components/ArtPost/ArtPost';
import { artApi } from '../../api/artApi';

export default function ArtView() {
    const { id } = useParams();
    const [art, setArt] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showUpBtn, setShowUpBtn] = useState(false);
    const artPostRef = useRef(null);
    const isLoadingRef = useRef(false);

    useEffect(() => {
        const loadArt = async () => {
            if (isLoadingRef.current) return;
            
            isLoadingRef.current = true;
            try {
                setLoading(true);
                setError(null);
                
                console.log('Loading art with ID:', id);
                const data = await artApi.getArtById(id);
                console.log('Loaded art data:', data);
                
                if (!data) throw new Error('Art not found');
                
                setArt(data);
            } catch (err) {
                console.error('Error loading art:', err);
                setError(err.message);
                setArt(null);
            } finally {
                setLoading(false);
                isLoadingRef.current = false;
            }
        };

        if (id) {
            loadArt();
        }
        
        return () => {
            isLoadingRef.current = false;
        };
    }, [id]);

    useEffect(() => {
        const handleScroll = () => {
            if (!artPostRef.current) return;

            const artPostTop = artPostRef.current.offsetTop;
            const scrollPosition = window.scrollY;

            setShowUpBtn(scrollPosition > artPostTop);
        };

        window.addEventListener('scroll', handleScroll);
        setTimeout(handleScroll, 100); // Небольшая задержка
        
        return () => window.removeEventListener('scroll', handleScroll);
    }, [art]);

    const scrollToTop = () => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    if (loading) return <div className={styles.container}>Загрузка...</div>;
    if (error) return <div className={styles.container}>Ошибка: {error}</div>;
    if (!art) return <div className={styles.container}>Арт не найден</div>;

    return (
        <div className={styles.container}>
            {/* Убрали лишнюю информацию об авторе, так как она уже в ArtPost */}
            <div ref={artPostRef}>
                <ArtPost 
                    key={`art-${art.id}`} 
                    artId={art.id}
                    image={art.imageUrl}
                    description={art.description}
                    tags={art.tags}
                    owner={art.author} // Автор передается в ArtPost
                />
            </div>

            {showUpBtn && (
                <button 
                    className={styles.upBtn}
                    onClick={scrollToTop}
                    aria-label="Наверх"
                >
                    <img src={UpIcon} alt="up-icon" className={styles.icon}/>
                </button>
            )}

            <div className={styles.comments}>
                {/* <Comment /> */}
            </div>
        </div>
    );
}