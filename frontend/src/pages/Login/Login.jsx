import { useState } from 'react';
import styles from './Login.module.css';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';

export default function Login() {

    const [error, setError] = useState(false);

    // Заглушка (отправка формы)
    const handleLogin = () => {
        // Здесь должна быть логика проверки логина/пароля
        // Например, вызов API и обработка ответа
        setError(true);
    };

    const resetError = () => {
        if (error) setError(false);
    };

    return (
        <>
            <div className={styles.form}>
                <span className={styles.text}>Вход</span>

                <div className={styles.inputGroup}>
                    <input 
                        type="text" 
                        placeholder='Логин/email' 
                        onChange={resetError} 
                    />
                    <input 
                        type="password" 
                        placeholder='Пароль' 
                        onChange={resetError} 
                    />
                </div>

                {error && (
                    <span className={styles.errorText}>
                        Неверный логин или пароль
                    </span>
                )}

                <DefaultBtn 
                    text={'Войти'} 
                    className={styles.loginBtn} 
                    onClick={handleLogin}
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