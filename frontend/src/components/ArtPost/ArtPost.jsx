import styles from './ArtPost.module.css';
import LikeBtn from '../LikeBtn/LikeBtn';

export default function ArtPost() {
    return(
        <div className={styles.artCard}>
            {/* Изображение */}
            <div className={styles.artImage}></div>

            {/* Тэги и описание */}
            <div className={styles.content}>
                <div className={styles.tags}>
                    #kfldfk #dkslk #dfjsldf #dkfldskf
                </div>

                <div className={styles.description}>
                    mamds podvvv;d|dspcdosk fdfldskf;dlfsd ;fkds;fdkf;fdlkfdfkdl fkld lf dkfldfk ldl kdfifk
                </div>


                <div className={styles.likeBadge}>
                    <LikeBtn />
                </div>
            </div>
        </div>
    )
}