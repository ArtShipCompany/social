import styles from './CoopBoard.module.css';
import CoopBoardIcon from '../../assets/coop-board-icon.svg';

export const CoopBoard = () => (
  <button className={styles.coopBoard}>
    <img src={CoopBoardIcon} alt="coop-board-icon" width="120" height="120"/>
    <span className={styles.text}>Общая доска</span>
  </button>
);

export default CoopBoard
