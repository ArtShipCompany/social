import styles from './Home.module.css';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import BoardCard from '../../components/BoardCard/BoardCard';
import ArtCard from '../../components/ArtCard/ArtCard';
import img1 from '../../assets/mock-images/джейхины.jpg';
import img2 from '../../assets/mock-images/клоризли.jpg';
import img3 from '../../assets/mock-images/софтикиэимики.jpg';
import img4 from '../../assets/mock-images/biliie.jpg';
import img5 from '../../assets/mock-images/pfp.jpg';
import img6 from '../../assets/mock-images/wenclair.jpg';
const mockImages = [img1, img2, img3, img4, img5, img6];
import SearchIcon from '../../assets/search-icon.svg';
import { TEXTS } from '../../assets/texts';

export default function Home() {

    return (
        <>
            <div className={styles.boards}>
                <span className={styles.text}>
                    {TEXTS.boards.private}
                </span>

                <div className={styles.cardsContainer}>
                    <BoardCard isPrivate={true} />
                    <BoardCard isPrivate={false} />
                </div>

                <span className={styles.text}>
                    {TEXTS.boards.public}
                </span>
            </div>
            
            <div className={styles.search}>
                <div className={styles.searchInputWrapper}>
                    <img src={SearchIcon} alt="Поиск" className={styles.icon} />
                    <input type="text" placeholder="Поиск..." className={styles.searchInput} />
                </div>
            </div>

            <div className={styles.feed}>
                {Array.from({ length: 30 }).map((_, i) => {
                    const image = mockImages[i % mockImages.length];
                    return <ArtCard key={i} image={image} showLikeButton={true} />;
                })}
            </div>

            <DefaultBtn text={'Показать ещё'} />

        </>
    );
};
