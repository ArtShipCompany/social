import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import styles from './Login.module.css';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import Input from '../../components/Input/Input';
import PasswordInput from '../../components/InputPassword/InputPassword';
import { useAuth } from '../../contexts/AuthContext';

export default function Login() {
    const navigate = useNavigate();
    const location = useLocation();
    const { login } = useAuth();
    
    const message = location.state?.message || '';
    
    const [formData, setFormData] = useState({
        identifier: '', 
        password: ''
    });

    const [errors, setErrors] = useState({
        identifier: '',
        password: '',
        form: ''
    });

    const [touched, setTouched] = useState({
        identifier: false,
        password: false
    });

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [showSuccessMessage, setShowSuccessMessage] = useState(!!message);

    const validateIdentifier = (value) => {
        if (!value) return 'Логин обязателен';
        if (value.length < 3) return 'Минимум 3 символа';
        return '';
    };

    const validatePassword = (value) => {
        if (!value) return 'Пароль обязателен';
        if (value.length < 6) return 'Пароль должен быть не менее 6 символов';
        return '';
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        
        if (errors[name] || errors.form) {
            setErrors(prev => ({ ...prev, [name]: '', form: '' }));
        }
        
        if (showSuccessMessage) {
            setShowSuccessMessage(false);
        }
    };

    const handleBlur = (e) => {
        const { name, value } = e.target;
        setTouched(prev => ({ ...prev, [name]: true }));
        
        let error = '';
        switch (name) {
            case 'identifier':
                error = validateIdentifier(value);
                break;
            case 'password':
                error = validatePassword(value);
                break;
            default:
                break;
        }
        
        setErrors(prev => ({ ...prev, [name]: error }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        const newErrors = {
            identifier: validateIdentifier(formData.identifier),
            password: validatePassword(formData.password),
            form: ''
        };
        
        setErrors(newErrors);
        setTouched({
            identifier: true,
            password: true
        });
        
        const hasErrors = Object.values(newErrors).some(error => error !== '');
        if (hasErrors) {
            return;
        }
        
        setIsSubmitting(true);
        setErrors(prev => ({ ...prev, form: '' }));
        
        try {
            console.log('Отправка данных для входа:', {
                identifier: formData.identifier,
                password: formData.password
            });
            
            // Используем метод login из AuthContext
            const result = await login({
                identifier: formData.identifier,
                password: formData.password
            });
            
            console.log('Результат входа:', result);
            
            if (!result.success) {
                throw new Error(result.error || 'Ошибка входа');
            }
            
            console.log('Успешный вход, пользователь установлен в AuthContext');
            
            // Перенаправляем на главную страницу или профиль
            const from = location.state?.from?.pathname || '/me';
            navigate(from, { replace: true });
            
        } catch (error) {
            console.error('Ошибка входа:', error);
            
            let errorMessage = 'Ошибка при входе';
            
            if (error.message.includes('401') || 
                error.message.toLowerCase().includes('invalid credentials') ||
                error.message.toLowerCase().includes('неверный') ||
                error.message.toLowerCase().includes('неправильный')) {
                errorMessage = 'Неверный логин или пароль';
            } else if (error.message.includes('404') || 
                       error.message.toLowerCase().includes('not found') ||
                       error.message.toLowerCase().includes('не найден')) {
                errorMessage = 'Пользователь не найден';
            } else if (error.message.includes('400')) {
                errorMessage = 'Некорректные данные';
            } else if (error.message.includes('NetworkError') || 
                       error.message.includes('Failed to fetch')) {
                errorMessage = 'Не удалось подключиться к серверу. Проверьте соединение';
            } else if (error.message.includes('403') || 
                       error.message.toLowerCase().includes('locked') ||
                       error.message.toLowerCase().includes('заблокирован')) {
                errorMessage = 'Аккаунт заблокирован';
            } else {
                errorMessage = error.message || 'Неизвестная ошибка';
            }
            
            setErrors(prev => ({ ...prev, form: errorMessage }));
            
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <>
            <div className={styles.form}>
                <span className={styles.text}>Вход</span>

                {showSuccessMessage && message && (
                    <div className={styles.successMessage}>
                        {message}
                    </div>
                )}

                {errors.form && (
                    <div className={styles.formError}>
                        {errors.form}
                    </div>
                )}

                <form onSubmit={handleSubmit} className={styles.inputGroup}>
                    <Input
                        name="identifier"
                        type="text"
                        placeholder="Логин"
                        value={formData.identifier}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        error={errors.identifier && touched.identifier ? errors.identifier : ''}
                        disabled={isSubmitting}
                    />

                    <PasswordInput
                        name="password"
                        placeholder="Пароль"
                        value={formData.password}
                        onChange={handleChange}
                        onBlur={handleBlur}
                        error={errors.password && touched.password ? errors.password : ''}
                        disabled={isSubmitting}
                    />

                    <DefaultBtn 
                        text={isSubmitting ? "Вход..." : "Войти"}
                        className={styles.loginBtn} 
                        type="submit"
                        disabled={isSubmitting}
                    />
                    
                    <p className={styles.footerText}>
                        Нет аккаунта?{' '}
                        <Link to="/register" className={styles.link}>
                            Зарегистрироваться
                        </Link>
                    </p>

                    <p className={styles.footerText}>
                        <Link to="/forgot-password" className={styles.link}>
                            Забыли пароль?
                        </Link>
                    </p>
                </form>
            </div>
        </>
    );
}