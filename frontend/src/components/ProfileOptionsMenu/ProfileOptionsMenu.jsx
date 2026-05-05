import { useRef } from 'react';
import styles from './ProfileOptionsMenu.module.css';
import ellipsisIcon from '../../assets/ellipsis-icon.svg';

export default function ProfileOptionsMenu({ 
  isOpen, 
  onToggle, 
  options = [] 
}) {
  const menuRef = useRef(null);

  return (
    <div ref={menuRef} className={styles.container} style={{ position: 'relative' }}>
      <button
        className={styles.ellipsisBtn}
        onClick={onToggle}
        aria-expanded={isOpen}
        aria-haspopup="menu"
      >
        <img src={ellipsisIcon} alt="Дополнительные опции" />
      </button>

      {isOpen && (
        <div className={styles.menu}>
          {options.map((option, index) => (
            <button
              key={option.key || index}
              className={`${styles.menuItem} ${option.className || ''}`}
              onClick={(e) => {
                e.stopPropagation();
                option.onClick?.();
                if (option.closeOnClick !== false) {
                  onToggle();
                }
              }}
              title={option.title}
              disabled={option.disabled}
            >
              <img 
                src={option.icon} 
                alt={option.alt || option.title} 
                className={styles.icon}
              />
            </button>
          ))}
        </div>
      )}
    </div>
  );
}