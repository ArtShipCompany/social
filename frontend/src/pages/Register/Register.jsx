import { useState } from 'react';
import { Link } from 'react-router-dom';
import styles from './Register.module.css';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import Input from '../../components/Input/Input';
import PasswordInput from '../../components/InputPassword/InputPassword';

export default function Register() {

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
        confirmPassword: ''
    });

    const [touched, setTouched] = useState({
        login: false,
        email: false,
        password: false,
        confirmPassword: false
    });

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
        
        if (errors[name]) {
            setErrors(prev => ({ ...prev, [name]: '' }));
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

    const handleSubmit = (e) => {
        e.preventDefault();
        
        const newErrors = {
            login: validateLogin(formData.login),
            email: validateEmail(formData.email),
            password: validatePassword(formData.password),
            confirmPassword: validateConfirmPassword(formData.confirmPassword, formData.password)
        };
        
        setErrors(newErrors);
        setTouched({
            login: true,
            email: true,
            password: true,
            confirmPassword: true
        });
        
        const hasErrors = Object.values(newErrors).some(error => error !== '');
        if (!hasErrors) {
            console.log('Форма валидна, отправляем данные:', formData);
        }
    };

    return (
    <>
        <div className={styles.form}>
            <span className={styles.text}>Регистрация</span>

            <form onSubmit={handleSubmit} className={styles.inputGroup}>
                <Input
                    name="login"
                    placeholder="Логин"
                    value={formData.login}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={errors.login && touched.login ? errors.login : ''}
                />

                <Input
                    name="email"
                    type="email"
                    placeholder="email@example.com"
                    value={formData.email}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={errors.email && touched.email ? errors.email : ''}
                />

                <PasswordInput
                    name="password"
                    placeholder="Пароль"
                    value={formData.password}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={errors.password && touched.password ? errors.password : ''}
                />

                <PasswordInput
                    name="confirmPassword"
                    placeholder="Повторите пароль"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    onBlur={handleBlur}
                    error={errors.confirmPassword && touched.confirmPassword ? errors.confirmPassword : ''}
                />

                <DefaultBtn text="Зарегестрироваться" className={styles.regBtn} type="submit" />
                
                <p className={styles.footerText}>
                    Есть аккаунт?{' '}
                    <Link to="/me" className={styles.link}>
                        Войти
                    </Link>
                </p>
            </form>
        </div>
    </>
  );
}