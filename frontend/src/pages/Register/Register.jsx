import styles from './Register.module.css';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import PasswordInput from '../../components/InputPassword/InputPassword';

export default function Register() {
    return (
        <>
        <div className={styles.form}>
            <span className={styles.text}>Регистрация</span>

            <div className={styles.inputGroup}>
                <input type="text" placeholder='Логин' />
                <input type="email" placeholder='email@example.com' />
                <PasswordInput placeholder="Пароль" />
                <PasswordInput placeholder="Повторите пароль" />

            </div>

            <DefaultBtn 
                text={'Зарегестрироваться'} 
                className={styles.regBtn} 
            />

            <p className={styles.footerText}>
                Есть аккаунт?{' '}
                <span
                    className={styles.link}
                    // onClick={() => navigate('/register')}
                >
                    Войти
                </span>
            </p>

        </div>
        </>
    );
}