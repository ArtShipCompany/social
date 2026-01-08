import styles from './BoardCard.module.css';

import CoopBoardIcon from '../../assets/coop-board-icon.svg';
import PrivateBoardIcon from '../../assets/private-board-icon.svg';
import PrivateIcon from '../../assets/private-icon.svg';

export default function BoardCard({ onClick, isPrivate }) {

    if (isPrivate) {
        return (
            <button className={styles.board} onClick={onClick}>
                <img src={PrivateIcon} alt="private-icon" width="30" height="30" className={styles.lockIcon}/>
                <div className={styles.content}>
                    <img src={PrivateBoardIcon} alt="private-board-icon" width="120" height="120"/>
                    <span className={styles.text}>Приватная доска</span>
                </div>
            </button>
        )
    }

    return (
        <button className={styles.board} onClick={onClick}>
            <div className={styles.content}>
                <img src={CoopBoardIcon} alt="coop-board-icon" width="120" height="120"/>
                <span className={styles.text}>Общая доска</span>
            </div>
        </button>
    )

}