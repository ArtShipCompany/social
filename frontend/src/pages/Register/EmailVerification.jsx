import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import styles from './Verification.module.css';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import { useAuth } from '../../contexts/AuthContext';

export default function EmailVerification() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { verifyEmail } = useAuth();
    
    const [status, setStatus] = useState('loading'); // loading, success, error
    const [message, setMessage] = useState('');
    const [userData, setUserData] = useState(null);

    useEffect(() => {
        const token = searchParams.get('token');
        
        if (!token) {
            setStatus('error');
            setMessage('❌ Токен не найден в ссылке');
            return;
        }

        handleVerify(token);
    }, [searchParams]);

    const handleVerify = async (token) => {
        try {
            setStatus('loading');
            setMessage('⏳ Проверяем токен...');
            
            const result = await verifyEmail(token);
            
            if (result.success) {
                setStatus('success');
                setMessage('✅ Email успешно подтвержден!');
                setUserData(result.data);
                
                // Очищаем localStorage от pending email
                localStorage.removeItem('pendingVerificationEmail');
                
                // Через 3 секунды редиректим на логин
                setTimeout(() => {
                    navigate('/login', { 
                        replace: true,
                        state: { 
                            message: 'Email подтвержден! Теперь вы можете войти.',
                            success: true
                        }
                    });
                }, 3000);
            } else {
                setStatus('error');
                setMessage(result.error || 'Не удалось подтвердить email');
            }
        } catch (err) {
            console.error('Ошибка верификации:', err);
            setStatus('error');
            
            let errorMsg = 'Неизвестная ошибка';
            if (err.status === 400) {
                errorMsg = '❌ Токен недействителен или истек';
            } else if (err.message) {
                errorMsg = `❌ ${err.message}`;
            }
            
            setMessage(errorMsg);
        }
    };

    const handleResendLink = () => {
        navigate('/register');
    };

    return (
        <div className={styles.container}>
            <div className={styles.card}>
                {status === 'loading' && (
                    <>
                        <div className={`${styles.icon} ${styles.loading}`}>⏳</div>
                        <h1 className={styles.title}>Проверка email...</h1>
                        <p className={styles.message}>{message}</p>
                        <div className={styles.spinner}></div>
                    </>
                )}

                {status === 'success' && (
                    <>
                        <div className={`${styles.icon} ${styles.success}`}>✅</div>
                        <h1 className={styles.title}>Email подтвержден!</h1>
                        <p className={styles.message}>{message}</p>
                        <p className={styles.redirect}>
                            Перенаправляем на страницу входа...
                        </p>
                        <Link to="/login" className={styles.loginLink}>
                            Перейти ко входу сейчас
                        </Link>
                    </>
                )}

                {status === 'error' && (
                    <>
                        <div className={`${styles.icon} ${styles.error}`}>❌</div>
                        <h1 className={styles.title}>Ошибка подтверждения</h1>
                        <p className={styles.message}>{message}</p>
                        
                        <div className={styles.actions}>
                            <DefaultBtn 
                                text="Запросить новую ссылку"
                                onClick={handleResendLink}
                                className={styles.resendBtn}
                            />
                            
                            <Link to="/login" className={styles.backLink}>
                                Вернуться ко входу
                            </Link>
                        </div>
                    </>
                )}
            </div>
        </div>
    );
}