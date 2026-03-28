import { useState, useEffect } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import styles from './Verification.module.css';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import { useAuth } from '../../contexts/AuthContext';

export default function VerificationSent() {
    const location = useLocation();
    const navigate = useNavigate();
    const { resendVerification } = useAuth();
    
    const [email, setEmail] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    const [countdown, setCountdown] = useState(60);

    useEffect(() => {
        // Получаем email из state или localStorage
        const userEmail = location.state?.email || localStorage.getItem('pendingVerificationEmail');
        if (userEmail) {
            setEmail(userEmail);
        } else {
            navigate('/register');
        }

        // Таймер обратного отсчета
        const timer = setInterval(() => {
            setCountdown(prev => {
                if (prev <= 1) {
                    clearInterval(timer);
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);

        return () => clearInterval(timer);
    }, [location.state, navigate]);

    const handleResend = async () => {
        if (!email || countdown > 0) return;

        setIsLoading(true);
        setError('');
        setMessage('');
        setCountdown(60);

        try {
            const result = await resendVerification(email);
            
            if (result.success) {
                setMessage('✅ Письмо отправлено повторно! Проверьте ваш email.');
            } else {
                setError(result.error || 'Не удалось отправить письмо');
            }
        } catch (err) {
            console.error('Ошибка при повторной отправке:', err);
            setError(err.message || 'Не удалось отправить письмо');
        } finally {
            setIsLoading(false);
        }
    };

    const handleBackToLogin = () => {
        navigate('/login', { replace: true });
    };

    return (
        <div className={styles.container}>
                
                <h1 className={styles.title}>Подтвердите ваш email</h1>
                
                <p className={styles.description}>
                    Мы отправили письмо с ссылкой для подтверждения на адрес:
                </p>
                
                <span className={styles.email}>{email}</span>
                
                <div className={styles.info}>
                    <p>Перейдите по ссылке в письме, чтобы активировать аккаунт</p>
                    <p>Не забудьте проверить папку "Спам"</p>
                </div>

                {message && <div className={styles.successMessage}>{message}</div>}
                {error && <div className={styles.errorMessage}>{error}</div>}

                <div className={styles.actions}>
                    <DefaultBtn 
                        text={countdown > 0 ? `Отправить повторно (${countdown}с)` : 'Отправить письмо повторно'}
                        onClick={handleResend}
                        disabled={isLoading || countdown > 0}
                        className={styles.resendBtn}
                    />
                    
                    <DefaultBtn 
                        text={'Вернуться ко входу'}
                        onClick={handleBackToLogin}
                        className={styles.backBtn}
                    />
                </div>
        </div>
    );
}
