import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import styles from './Verification.module.css';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import { useAuth } from '../../contexts/AuthContext';

export default function EmailVerification() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { verifyEmail, resendVerification } = useAuth();
    
    const [email, setEmail] = useState('');
    const [isResending, setIsResending] = useState(false);
    const [resendMessage, setResendMessage] = useState('');
    const [resendError, setResendError] = useState('');
    const [status, setStatus] = useState('loading'); // loading, success, error
    const [message, setMessage] = useState('');
    const [userData, setUserData] = useState(null);


    useEffect(() => {
        const token = searchParams.get('token');
        
        const storedEmail = localStorage.getItem('pendingVerificationEmail');
        if (storedEmail) {
            setEmail(storedEmail);
        }

        if (!token) {
            setStatus('error');
            setMessage('Токен не найден в ссылке');
            return;
        }

        handleVerify(token);
    }, [searchParams]);

    const handleVerify = async (token) => {
        try {
            setStatus('loading');
            setMessage('Проверяем токен...');
            
            const result = await verifyEmail(token);
            
            if (result.success) {
                setStatus('success');
                setMessage('Email успешно подтвержден!');
                setUserData(result.data);
                
                // Очищаем localStorage от pending email
                localStorage.removeItem('pendingVerificationEmail');
                
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
                errorMsg = 'Токен недействителен или истек';
            } else if (err.message) {
                errorMsg = `${err.message}`;
            }
            
            setMessage(errorMsg);
        }
    };

    const handleResendLink = async () => {
        const emailToResend = email || localStorage.getItem('pendingVerificationEmail');
        
        if (!emailToResend) {
            setResendError('Не удалось определить email. Вернитесь на страницу регистрации.');
            return;
        }

        setIsResending(true);
        setResendMessage('');
        setResendError('');

        try {
            const result = await resendVerification(emailToResend);
            
            if (result.success) {
                setResendMessage(`✅ Новая ссылка отправлена на ${emailToResend}`);
            } else {
                setResendError(result.error || 'Не удалось отправить письмо');
            }
        } catch (err) {
            console.error('Ошибка при повторной отправке:', err);
            setResendError(err.message || 'Не удалось отправить письмо');
        } finally {
            setIsResending(false);
        }
    };

    return (
        <div className={styles.container}>
            <div className={styles.card}>
                {status === 'loading' && (
                    <>
                        <h1 className={styles.title}>Проверка email...</h1>
                        <p className={styles.message}>{message}</p>
                        <div className={styles.spinner}></div>
                    </>
                )}

                {status === 'success' && (
                    <>
                        <h1 className={styles.title}>Email подтвержден!</h1>
                        <p className={styles.message}>{message}</p>
                        <p className={styles.redirect}>
                            Перенаправляем на страницу входа...
                        </p>
                    </>
                )}

                {status === 'error' && (
                    <>
                        <div className={`${styles.icon} ${styles.error}`}></div>
                        <h1 className={styles.title}>Ошибка подтверждения</h1>
                        <p className={styles.message}>{message}</p>
                        
                        {resendMessage && (
                            <p className={styles.successMessage}>{resendMessage}</p>
                        )}
                        {resendError && (
                            <p className={styles.errorMessage}>{resendError}</p>
                        )}
                        
                        <div className={styles.action}>
                            <DefaultBtn 
                                text={isResending ? "Отправляем..." : "Запросить новую ссылку"}
                                onClick={handleResendLink}
                                disabled={isResending}
                                className={styles.resendBtn}
                            />
                        </div>
                    </>
                )}
            </div>
        </div>
    );
}