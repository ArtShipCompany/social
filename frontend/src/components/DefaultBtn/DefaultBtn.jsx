import styles from './DefaultBtn.module.css';

export default function DefaultBtn({ text, onClick }) {
    return (
        <button 
            className={styles.btn}
            onClick={onClick}
        >
            {text}
        </button>
    );
}