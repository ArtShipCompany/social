import { useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import styles from './ArtView.module.css';
import UpIcon from '../../assets/up-icon.svg'
import ArtPost from '../../components/ArtPost/ArtPost';
import { mockArtsMap } from '../../mock-images/mockArts';
// Пока моковая функция — потом заменишь на fetch или axios
async function fetchArtById(id) {
  return new Promise(resolve => setTimeout(() => resolve(mockArtsMap[id]), 300));
}

export default function ArtView() {
    const { id } = useParams(); // ← получаем id из URL
    const [art, setArt] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [showUpBtn, setShowUpBtn] = useState(false);
    const artPostRef = useRef(null);

    useEffect(() => {
        const loadArt = async () => {
        try {
            setLoading(true);
            const data = await fetchArtById(id);
            if (!data) throw new Error('Art not found');
            setArt(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
        };

        if (id) loadArt();
    }, [id]);

    useEffect(() => {
        const handleScroll = () => {
            if (!artPostRef.current) return;

            const artPostTop = artPostRef.current.offsetTop;
            const scrollPosition = window.scrollY;

            if (scrollPosition > artPostTop) {
                setShowUpBtn(true);
            } else {
                setShowUpBtn(false);
            }
        };

        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, []);

    const scrollToTop = () => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    if (loading) return <div className={styles.container}>Загрузка...</div>;
    if (error) return <div className={styles.container}>Ошибка: {error}</div>;
    if (!art) return <div className={styles.container}>Арт не найден</div>;

    return (
        <div className={styles.container}>
            <div ref={artPostRef}>
                <ArtPost 
                    isOwner={false}
                    artId={art.id}
                    image={art.image}
                    description={art.description}
                    tags={art.tags}
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