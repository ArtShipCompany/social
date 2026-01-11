import styles from './ConfirmModal.module.css';
import DefaultBtn from '../DefaultBtn/DefaultBtn';

export default function ConfirmModal({ 
  isOpen, 
  onClose, 
  onConfirm, 
  title = 'Подтверждение', 
  message = 'Вы уверены?',
}) {
  if (!isOpen) return null;

  return (
    <div className={styles.overlay}>
      <div className={styles.modal}>
        <h3 className={styles.title}>{title}</h3>
        <p className={styles.message}>{message}</p>

        <div className={styles.buttons}>
          <DefaultBtn 
            text="Отмена" 
            onClick={onClose} 
            className={styles.cancelBtn}
          />
          <DefaultBtn 
            text="Удалить" 
            onClick={onConfirm} 
            className={styles.deleteBtn}
          />
        </div>
      </div>
    </div>
  );
}