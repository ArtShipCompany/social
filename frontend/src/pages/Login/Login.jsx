import styles from './Login.module.css';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';

export default function Login() {
    return (
        <>
        <div className={styles.form}>
            <span className={styles.text}>Вход</span>

            <div className={styles.inputGroup}>
                <input type="text" placeholder='Логин/email' />
                <input type="password" placeholder='Пароль' />
            </div>

            <DefaultBtn 
                text={'Войти'} 
                className={styles.loginBtn} 
            />

            <p className={styles.footerText}>
                Нет аккаунта?{' '}
                <span
                    className={styles.link}
                    // onClick={() => navigate('/register')}
                >
                    Зарегистрироваться
                </span>
            </p>

        </div>
        </>
    );
}