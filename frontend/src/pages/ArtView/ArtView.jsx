import { useState, useEffect, useRef } from 'react';

import img1 from '../../assets/mock-images/клоризли.jpg';
import img2 from '../../assets/mock-images/biliie.jpg';
import img3 from '../../assets/mock-images/pfp.jpg';
import img4 from '../../assets/mock-images/wenclair.jpg';
// const mockImages = [img1, img6, img3, img4];
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
        <div className={styles.container}>
            <div ref={artPostRef}>
                <ArtPost isOwner={true} image={img4}/> {/* здесь проверка пост пользавателя или нет */}
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