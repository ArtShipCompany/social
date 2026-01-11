import { useState } from 'react';
import styles from './InputPassword.module.css';
import SeeIcon from '../../assets/see-pass.svg';
import HideIcon from '../../assets/hide-pass.svg';

export default function PasswordInput({ 
  placeholder, 
  value, 
  onChange, 
  onBlur, 
  name, 
  error, 
  ...props 
}) {
  const [showPassword, setShowPassword] = useState(false);

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  return (
    <div className={styles.passwordContainer}>
      <input
        type={showPassword ? 'text' : 'password'}
        name={name}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        onBlur={onBlur}
        className={`${styles.input} ${error ? styles.inputError : ''}`}
        {...props}
      />
      <button
        type="button"
        className={styles.eyeButton}
        onClick={togglePasswordVisibility}
        aria-label={showPassword ? "Скрыть пароль" : "Показать пароль"}
      >
        <img
          src={showPassword ? SeeIcon : HideIcon}
          alt=""
          className={styles.eyeIcon}
        />
      </button>
      {error && <span className={styles.errorMessage}>{error}</span>}
    </div>
  );
}