import { useState } from 'react';
import styles from './InputPassword.module.css';
import SeeIcon from '../../assets/see-pass.svg';
import HideIcon from '../../assets/hide-pass.svg';

export default function InputPassword({ placeholder, ...props }) {
  const [showPassword, setShowPassword] = useState(false);

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  return (
    <div className={styles.passwordContainer}>
      <input
        type={showPassword ? 'text' : 'password'}
        placeholder={placeholder}
        className={styles.input}
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
    </div>
  );
}