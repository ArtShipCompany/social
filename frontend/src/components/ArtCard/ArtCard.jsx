import styles from './ArtCard.module.css';
import LikeBtn from '../LikeBtn/LikeBtn';

export default function ArtCard({showLikeButton}) {

    return (
        <div className={styles.card}>
            {/*<img> </img>*/}
            <LikeBtn showLikeButton={showLikeButton}/>
            
        </div>
    )
}