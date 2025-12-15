import styles from './Header.module.css';

export const Header = () => (
  <header className={styles.header}>
    <h1 className={styles.title}>ARTSHIP</h1>

    <div className={styles.buttons}>
        <button className={styles.button}>Регистрация</button>
        <button className={styles.button}>Войти</button>
    </div>

  </header>
);

export default Header