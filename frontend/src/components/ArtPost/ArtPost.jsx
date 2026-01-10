import styles from './ArtPost.module.css';
import LikeBtn from '../LikeBtn/LikeBtn';

export default function ArtPost() {
    return(
        <div className={styles.artWrapp}>
            <div className={styles.artImage}></div>

            <div className={styles.content}>
                <div className={styles.textContent}>
                    <div className={styles.tags}>
                        <span>#kfldfk #dkslk #dfjsldf #dkfldskf</span>
                    </div>
                    <span>mamds podvvv;d|dspcdosk fdfldskf;dlfsd ;fkds;fdkf;fdlkfdfkdl fkld lf dkfldfk ldl kdfifk</span>
                </div>
                    <LikeBtn className={styles.like}/>
            </div>
        </div>
    );
}