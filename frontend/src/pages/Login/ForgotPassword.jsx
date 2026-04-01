import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from './ForgotPassword.module.css';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import Input from '../../components/Input/Input';
import { useAuth } from '../../contexts/AuthContext';
import { useNotification } from '../../contexts/NotificationContext';

export default function ForgotPassword() {
    const navigate = useNavigate();
    const { forgotPassword } = useAuth();
    const notification = useNotification();
    
    const [email, setEmail] = useState('');
    const [errors, setErrors] = useState({ email: '', form: '' });
    const [touched, setTouched] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isSent, setIsSent] = useState(false);

    const validateEmail = (value) => {
        if (!value) return 'Email обязателен';
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(value)) return 'Некорректный email';
        return '';
    };

    const handleChange = (e) => {
        setEmail(e.target.value);
        if (errors.email || errors.form) {
            setErrors({ email: '', form: '' });
        }
    };

    const handleBlur = () => {
        setTouched(true);
        const error = validateEmail(email);
        setErrors(prev => ({ ...prev, email: error }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        const error = validateEmail(email);
        setErrors({ email: error, form: '' });
        setTouched(true);
        
        if (error) return;
        
        setIsSubmitting(true);
        
        try {
            const result = await forgotPassword(email);
            
            if (result.success) {
                setIsSent(true);
                notification.success('✅ Письмо для сброса пароля отправлено!', 4000);
            } else {
                setErrors(prev => ({ ...prev, form: result.error }));
                notification.error(result.error, 3000);
            }
        } catch (err) {
            console.error('Forgot password error:', err);
            setErrors(prev => ({ ...prev, form: err.message || 'Ошибка отправки' }));
            notification.error(err.message || 'Ошибка отправки', 3000);
        } finally {
            setIsSubmitting(false);
        }
    };

    if (isSent) {
        return (
            <div className={styles.container}>
                <div className={styles.card}>
                    <h1 className={styles.title}>Проверьте почту</h1>
                    <p className={styles.description}>
                        Мы отправили ссылку для сброса пароля на адрес:
                    </p>
                    <span className={styles.email}>{email}</span>
                    
                    <div className={styles.info}>
                        <p>Перейдите по ссылке в письме</p>
                        <p>Ссылка действительна 1 час</p>
                        <p>Проверьте папку "Спам", если не видите письмо</p>
                    </div>

                    <div className={styles.actions}>
                        <DefaultBtn 
                            text="Вернуться ко входу"
                            onClick={() => navigate('/login')}
                            className={styles.backBtn}
                        />
                        <button 
                            type="button"
                            className={styles.resendLink}
                            onClick={() => {
                                setIsSent(false);
                                setEmail('');
                                setTouched(false);
                            }}
                        >
                            Ввести другой email
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className={styles.container}>
            <div className={styles.form}>
                <span className={styles.text}>Забыли пароль?</span>
                
                {errors.form && (
                    <div className={styles.formError}>{errors.form}</div>
                )}

                <form onSubmit={handleSubmit} className={styles.inputGroup}>
                    <Input
                        name="email"
                        type="email"
                        placeholder="email@example.com"
                        value={email}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        error={errors.email && touched ? errors.email : ''}
                        disabled={isSubmitting}
                        autoComplete="email"
                        required
                    />

                    <div className={styles.submit}>
                        <DefaultBtn 
                            text={isSubmitting ? "Отправляем..." : "Отправить ссылку"}
                            className={styles.submitBtn} 
                            type="submit"
                            disabled={isSubmitting}
                        />
                        
                        <p className={styles.footerText}>
                            Вспомнили пароль?{' '}
                            <Link to="/login" className={styles.link}>
                                Войти
                            </Link>
                        </p>
                    </div>
                </form>
            </div>
        </div>
    );
}