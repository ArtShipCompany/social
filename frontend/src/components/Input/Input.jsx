import styles from './Input.module.css';

export default function Input({ 
  placeholder, 
  value, 
  onChange, 
  onBlur, 
  name, 
  type = 'text', 
  error, 
  ...props 
}) {
  return (
    <div className={styles.inputWrapper}>
      <input
        type={type}
        name={name}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        onBlur={onBlur}
        className={`${styles.input} ${error ? styles.inputError : ''}`}
        {...props}
      />
      {error && <span className={styles.errorMessage}>{error}</span>}
    </div>
  );
}
