import styles from './Login.module.css';

export default function Login() {
    return (
        <>
        <div className={styles.form}>
            <span className={styles.text}>Вход</span>
            <div className={styles.inputGroup}>
                <input type="text" placeholder='Логин/email' />
                <input type="password" placeholder='Пароль' />
            </div>
        </div>
        </>
    );
}