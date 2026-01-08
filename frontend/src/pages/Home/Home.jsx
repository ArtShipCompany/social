import styles from './Home.module.css';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import BoardCard from '../../components/BoardCard/BoardCard';
import ArtCard from '../../components/ArtCard/ArtCard';

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
                {Array.from({ length: 5 }).map((_, i) => (
                    <div key={i} className={styles.column}>
                        <ArtCard />
                        <ArtCard />
                        <ArtCard />
                    </div>
                ))}
            </div>

            <DefaultBtn text={'Показать ещё'} />

        </>
    );
};
