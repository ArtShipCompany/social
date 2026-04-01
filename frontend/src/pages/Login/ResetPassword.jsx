import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import styles from './ResetPassword.module.css';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import PasswordInput from '../../components/InputPassword/InputPassword';
import { useAuth } from '../../contexts/AuthContext';
import { useNotification } from '../../contexts/NotificationContext';

export default function ResetPassword() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { validateResetToken, resetPassword } = useAuth();
    const notification = useNotification();
    
    
    const [formData, setFormData] = useState({
        password: '',
        confirmPassword: ''
    });
    const [errors, setErrors] = useState({
        password: '',
        confirmPassword: '',
        form: ''
    });
    const [touched, setTouched] = useState({
        password: false,
        confirmPassword: false
    });
    
    const [isLoading, setIsLoading] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [tokenValid, setTokenValid] = useState(false);
    const [token, setToken] = useState('');

    const [hasResetPassword, setHasResetPassword] = useState(false);

    useEffect(() => {
        const tokenParam = searchParams.get('token');
        
        if (!tokenParam) {
            notification.error('Ссылка недействительна', 3000);
            navigate('/forgot-password', { replace: true });
            return;
        }
        
        setToken(tokenParam);
        checkToken(tokenParam);
    }, [searchParams, navigate, notification]);

    const checkToken = async (tokenValue) => {
        try {
            setIsLoading(true);
            const result = await validateResetToken(tokenValue);
            
            if (hasResetPassword) return;
            
            if (result.success) {
                setTokenValid(true);
            } else {
                setTokenValid(false);
                setErrors(prev => ({ ...prev, form: result.error }));
                notification.error(result.error, 4000);
            }
        } catch (err) {
            if (hasResetPassword) {
                console.log('Token check after password reset - ignoring');
                return;
            }
            
            console.error('Token validation error:', err);
            setTokenValid(false);
            setErrors(prev => ({ ...prev, form: 'Ссылка недействительна или истекла' }));
            notification.error('Ссылка недействительна или истекла', 4000);
        } finally {
            if (!hasResetPassword) {
                setIsLoading(false);
            }
        }
    };

    const validatePassword = (value) => {
        if (!value) return 'Пароль обязателен';
        if (value.length < 6) return 'Минимум 6 символов';
        if (!/[A-Za-z]/.test(value)) return 'Должна быть хотя бы одна буква';
        if (!/\d/.test(value)) return 'Должна быть хотя бы одна цифра';
        return '';
    };

    const validateConfirmPassword = (value, password) => {
        if (!value) return 'Подтвердите пароль';
        if (value !== password) return 'Пароли не совпадают';
        return '';
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        
        if (errors[name] || errors.form) {
            setErrors(prev => ({ ...prev, [name]: '', form: '' }));
        }
    };

    const handleBlur = (e) => {
        const { name, value } = e.target;
        setTouched(prev => ({ ...prev, [name]: true }));
        
        let error = '';
        if (name === 'password') {
            error = validatePassword(value);
        } else if (name === 'confirmPassword') {
            error = validateConfirmPassword(value, formData.password);
        }
        setErrors(prev => ({ ...prev, [name]: error }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        const newErrors = {
            password: validatePassword(formData.password),
            confirmPassword: validateConfirmPassword(formData.confirmPassword, formData.password),
            form: ''
        };
        
        setErrors(newErrors);
        setTouched({ password: true, confirmPassword: true });
        
        if (Object.values(newErrors).some(err => err !== '')) return;
        
        setIsSubmitting(true);
        
        try {
            const result = await resetPassword(token, formData.password);
            
            if (result.success) {
                setHasResetPassword(true);
                notification.success('✅ Пароль успешно изменен!', 3000);

                setTimeout(() => {
                    navigate('/login', { 
                        replace: true,
                        state: { message: 'Пароль изменен! Войдите с новым паролем.' }
                    });
                }, 100);
            } else {
                setErrors(prev => ({ ...prev, form: result.error }));
                notification.error(result.error, 3000);
            }
        } catch (err) {
            console.error('Reset password error:', err);
            setErrors(prev => ({ ...prev, form: err.message || 'Ошибка сброса пароля' }));
            notification.error(err.message || 'Ошибка сброса пароля', 3000);
        } finally {
            setIsSubmitting(false);
        }
    };

    if (isLoading) {
        return (
            <div className={styles.container}>
                <div className={styles.card}>
                    <div className={styles.spinner}></div>
                    <p>Проверяем ссылку...</p>
                </div>
            </div>
        );
    }

    if (!tokenValid) {
        return (
            <div className={styles.container}>
                <div className={styles.card}>
                    <div className={`${styles.icon} ${styles.error}`}>⚠️</div>
                    <h1 className={styles.title}>Ссылка недействительна</h1>
                    <p className={styles.message}>
                        {errors.form || 'Токен истёк или некорректен'}
                    </p>
                    <DefaultBtn 
                        text="Запросить новую ссылку"
                        onClick={() => navigate('/forgot-password')}
                        className={styles.retryBtn}
                    />
                </div>
            </div>
        );
    }

    // ✅ Форма сброса пароля
    return (
        <div className={styles.container}>
            <div className={styles.form}>
                <span className={styles.text}>Новый пароль</span>
                
                {errors.form && (
                    <div className={styles.formError}>{errors.form}</div>
                )}

                <form onSubmit={handleSubmit} className={styles.inputGroup}>
                    <PasswordInput
                        name="password"
                        placeholder="Новый пароль"
                        value={formData.password}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        error={errors.password && touched.password ? errors.password : ''}
                        disabled={isSubmitting}
                        autoComplete="new-password"
                        required
                    />

                    <PasswordInput
                        name="confirmPassword"
                        placeholder="Повторите пароль"
                        value={formData.confirmPassword}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        error={errors.confirmPassword && touched.confirmPassword ? errors.confirmPassword : ''}
                        disabled={isSubmitting}
                        autoComplete="new-password"
                        required
                    />

                    <div className={styles.submit}>
                        <DefaultBtn 
                            text={isSubmitting ? "Сохраняем..." : "Сменить пароль"}
                            className={styles.submitBtn} 
                            type="submit"
                            disabled={isSubmitting}
                        />
                    </div>
                </form>
            </div>
        </div>
    );
}