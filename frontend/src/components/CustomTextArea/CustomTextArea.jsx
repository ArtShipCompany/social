import { useState } from 'react';
import styles from './CustomTextArea.module.css';

export default function CustomTextArea({
    value,
    onChange,
    maxLength,
    placeholder,
    label,
    id,
}) {
    const handleInput = (e) => {
        e.target.style.height = 'auto';
        e.target.style.height = e.target.scrollHeight + 'px';
    };

    const handleChange = (e) => {
        console.log('📝 CustomTextArea onChange:', e.target.value);
        if (onChange) {
            onChange(e);
        }
    };

    return (
        <div className={styles.textareaWrapper}>
            {label && <label htmlFor={id}>{label}</label>}
            <div className={styles.textareaContainer}>
                <textarea
                    id={id}
                    value={value}
                    onChange={handleChange}
                    onInput={handleInput}
                    maxLength={maxLength}
                    placeholder={placeholder}
                    className={styles.textarea}
                />
                <div className={styles.charCount}>
                    {value.length}/{maxLength}
                </div>
            </div>
        </div>
    );
}