import styles from './Header.module.css';

function Header() {
    return (
        <div className={styles.header}>

            <a className={styles.link} href="/">
                <span className={styles.text}>ARTSHIP</span>
            </a>

            <div className={styles.container}>
                <button className={styles.cover}>
                    Войти
                </button>
                <button className={styles.cover}>
                    Зарегистрироваться
                </button>
            </div>
        </div>
    );
}

export default Header;