import styles from './DefaultBtn.module.css';

export default function DefaultBtn({ text, onClick, className, type }) {
    return (
        // возможно тип в button на что-то влияет, надо чекнуть, этот тип только в Register юзался как submit
        // наверн нужно для отправок формы 
        <button 
            className={`${styles.btn} ${className || ''}`}
            onClick={onClick}
            type={type} 
        >
            {text}
        </button>
    );
}