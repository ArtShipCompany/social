import { forwardRef } from 'react';
import styles from './CustomTextArea.module.css';

export default forwardRef(function CustomTextArea({
    value,
    onChange,
    maxLength,
    placeholder,
    label,
    id,
}, ref) {
    const handleInput = (e) => {
        e.target.style.height = 'auto';
        e.target.style.height = e.target.scrollHeight + 'px';
    };

    const handleChange = (e) => {
        if (onChange) {
            onChange(e);
        }
    };

    return (
        <div className={styles.textareaWrapper}>
            {label && <label htmlFor={id}>{label}</label>}
            <div className={styles.textareaContainer}>
                <textarea
                    ref={ref}
                    id={id}
                    value={value}
                    onChange={handleChange}
                    onInput={handleInput}
                    maxLength={maxLength}
                    placeholder={placeholder}
                    className={styles.textarea}
                />
                <div className={styles.charCount}>
                    {value?.length || 0}/{maxLength}
                </div>
            </div>
        </div>
    );
});