import styles from './DefaultBtn.module.css';

export default function DefaultBtn({ text, onClick, className, type }) {
    return (
        <button 
            className={`${styles.btn} ${className || ''}`}
            onClick={onClick}
            type={type}
        >
            {text}
        </button>
    );
}