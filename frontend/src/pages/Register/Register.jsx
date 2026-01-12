import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from './Register.module.css';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import Input from '../../components/Input/Input';
import PasswordInput from '../../components/InputPassword/InputPassword';
import { authApi } from '../../api/authApi';

export default function Register() {
    const navigate = useNavigate();
    
    const [formData, setFormData] = useState({
        login: '',
        email: '',
        password: '',
        confirmPassword: ''
    });

    const [errors, setErrors] = useState({
        login: '',
        email: '',
        password: '',
        confirmPassword: '',
        form: '' 
    });

    const [touched, setTouched] = useState({
        login: false,
        email: false,
        password: false,
        confirmPassword: false
    });

    const [isSubmitting, setIsSubmitting] = useState(false);

    const validateLogin = (value) => {
        if (!value) return 'Логин обязателен';
        if (value.length < 3) return 'Логин должен быть не менее 3 символов';
        if (value.length > 30) return 'Логин не может быть длиннее 30 символов';
        
        const regex = /^[A-Za-z0-9._]+$/;
        if (!regex.test(value)) return 'Логин может содержать только буквы, цифры, . и _';
        
        const hasLetter = /[A-Za-z]/.test(value);
        if (!hasLetter) return 'Логин должен содержать хотя бы одну букву';
        
        return '';
    };

    const validateEmail = (value) => {
        if (!value) return 'Email обязателен';
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(value)) return 'Некорректный email';
        return '';
    };

    const validatePassword = (value) => {
        if (!value) return 'Пароль обязателен';
        if (value.length < 6) return 'Пароль должен быть не менее 6 символов';
        
        const hasLetter = /[A-Za-z]/.test(value);
        if (!hasLetter) return 'Пароль должен содержать хотя бы одну букву';
        
        const hasDigit = /\d/.test(value);
        if (!hasDigit) return 'Пароль должен содержать хотя бы одну цифру';
        
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
        switch (name) {
        case 'login':
            error = validateLogin(value);
            break;
        case 'email':
            error = validateEmail(value);
            break;
        case 'password':
            error = validatePassword(value);
            break;
        case 'confirmPassword':
            error = validateConfirmPassword(value, formData.password);
            break;
        default:
            break;
        }
        
        setErrors(prev => ({ ...prev, [name]: error }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        const newErrors = {
            login: validateLogin(formData.login),
            email: validateEmail(formData.email),
            password: validatePassword(formData.password),
            confirmPassword: validateConfirmPassword(formData.confirmPassword, formData.password),
            form: ''
        };
        
        setErrors(newErrors);
        setTouched({
            login: true,
            email: true,
            password: true,
            confirmPassword: true
        });
        
        const hasErrors = Object.values(newErrors).some(error => error !== '');
        if (hasErrors) {
            return;
        }
        
        setIsSubmitting(true);
        setErrors(prev => ({ ...prev, form: '' }));
        
        try {
            console.log('Отправка данных на регистрацию:', {
                login: formData.login,
                email: formData.email,
                password: formData.password
            });
            
            // Отправляем данные на бэкенд
            const response = await authApi.register({
                login: formData.login,
                email: formData.email,
                password: formData.password
            });
            
            console.log('Успешная регистрация:', response);
            
            // Если регистрация успешна, сразу логиним пользователя
            try {
                const loginResponse = await authApi.login({
                    login: formData.login,
                    password: formData.password
                });
                
                console.log('Автоматический вход после регистрации:', loginResponse);
                
                // Сохраняем токены в localStorage
                if (loginResponse.accessToken && loginResponse.refreshToken) {
                    localStorage.setItem('accessToken', loginResponse.accessToken);
                    localStorage.setItem('refreshToken', loginResponse.refreshToken);
                    
                    // Можно сохранить информацию о пользователе
                    localStorage.setItem('user', JSON.stringify(loginResponse.user || response));
                }
                
                // Перенаправляем на главную страницу или профиль
                navigate('/me');
                
            } catch (loginError) {
                console.log('Регистрация успешна, но автоматический вход не удался:', loginError);
                // Перенаправляем на страницу логина
                navigate('/login', { 
                    state: { 
                        message: 'Регистрация успешна! Теперь войдите в систему',
                        email: formData.email 
                    } 
                });
            }
            
        } catch (error) {
            console.error('Ошибка регистрации:', error);
            
            let errorMessage = 'Ошибка при регистрации';
            
            if (error.message.includes('409') || error.message.toLowerCase().includes('exist')) {
                errorMessage = 'Пользователь с таким логином или email уже существует';
            } else if (error.message.includes('400')) {
                errorMessage = 'Некорректные данные';
            } else if (error.message.includes('NetworkError') || error.message.includes('Failed to fetch')) {
                errorMessage = 'Не удалось подключиться к серверу. Проверьте соединение';
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
            <span className={styles.text}>Регистрация</span>

            {/* Общая ошибка формы */}
            {errors.form && (
                <div className={styles.formError}>
                    {errors.form}
                </div>
            )}

            <form onSubmit={handleSubmit} className={styles.inputGroup}>
                <Input
                    name="login"
                    placeholder="Логин"
                    value={formData.login}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={errors.login && touched.login ? errors.login : ''}
                    disabled={isSubmitting}
                />

                <Input
                    name="email"
                    type="email"
                    placeholder="email@example.com"
                    value={formData.email}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={errors.email && touched.email ? errors.email : ''}
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

                <PasswordInput
                    name="confirmPassword"
                    placeholder="Повторите пароль"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={errors.confirmPassword && touched.confirmPassword ? errors.confirmPassword : ''}
                    disabled={isSubmitting}
                />

                <DefaultBtn 
                    text={isSubmitting ? "Регистрация..." : "Зарегистрироваться"}
                    className={styles.regBtn} 
                    type="submit"
                    disabled={isSubmitting}
                />
                
                <p className={styles.footerText}>
                    Есть аккаунт?{' '}
                    <Link to="/login" className={styles.link}>
                        Войти
                    </Link>
                </p>
            </form>
        </div>
    </>
  );
}