import styles from './Home.module.css';
import BoardCard from '../../components/BoardCard/BoardCard';
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

            </div>

            <div className={styles.feed}>

            </div>

            <button className={styles.button}>
                Показать ещё
            </button>

        </>
    );
};
