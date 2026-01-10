import { useState, useEffect, useRef } from 'react';

import styles from './ArtView.module.css';

import UpIcon from '../../assets/up-icon.svg'
import ArtPost from '../../components/ArtPost/ArtPost';

export default function ArtView() {

    const [showUpBtn, setShowUpBtn] = useState(false);
    const artPostRef = useRef(null);

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

    return (
        <div className={styles.content}>
            <div ref={artPostRef}>
                <ArtPost isOwner={true}/> {/* здесь проверка пост пользавателя или нет */}
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
    )
}