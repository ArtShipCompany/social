import styles from './PrivateBoard.module.css';

import PrivateBoardIcon from '../../assets/private-board-icon.svg';
import PrivateIcon from '../../assets/private-icon.svg';

export const PrivateBoard = () => (
  <button className={styles.privateBoard}>
    <img src={PrivateIcon} alt="private-icon" width="30" height="30" className={styles.lockIcon}/>
    <div className={styles.content}>
        <img src={PrivateBoardIcon} alt="private-board-icon" width="120" height="120"/>
        <span className={styles.text}>Приватная доска</span>
    </div>
  </button>
);

export default PrivateBoard
