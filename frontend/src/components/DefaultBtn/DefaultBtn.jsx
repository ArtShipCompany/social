import styles from './DefaultBtn.module.css';

export default function DefaultBtn({ text, onClick, className }) {
    return (
        <button 
            className={`${styles.btn} ${className || ''}`}
            onClick={onClick}
        >
            {text}
        </button>
    );
}