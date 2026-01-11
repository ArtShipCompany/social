import { useState, useEffect, useRef } from 'react';
import styles from './ProfileOptionsMenu.module.css';
import privacyIcon from '../../assets/private-edit.svg';
import deleteIcon from '../../assets/delete-icon.svg';
import ellipsisIcon from '../../assets/ellipsis-icon.svg';

export default function ProfileOptionsMenu({ onPrivacyClick, onDeleteClick }) {
  const [isOpen, setIsOpen] = useState(false);
  const menuRef = useRef(null);

  const toggleMenu = () => setIsOpen(!isOpen);

  const handleClickOutside = (e) => {
    if (!e.target.closest(`.${styles.menu}`)) {
      setIsOpen(false);
    }
  };

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, []);

  return (
    <div ref={menuRef} style={{ position: 'relative' }}>
      <button
        className={styles.ellipsisBtn}
        onClick={toggleMenu}
        aria-expanded={isOpen}
        aria-haspopup="menu"
      >
        <img src={ellipsisIcon} alt="Дополнительные опции" />
      </button>

      {isOpen && (
        <div className={styles.menu}>
          <button
            className={styles.menuItem}
            onClick={() => {
              onPrivacyClick();
              setIsOpen(false);
            }}
          >
            <img src={privacyIcon} alt="Приватность" className={styles.icon} />
          </button>

          <button
            className={styles.menuItem}
            onClick={() => {
              onDeleteClick();
              setIsOpen(false);
            }}
          >
            <img src={deleteIcon} alt="Удалить" className={styles.icon} />
          </button>
        </div>
      )}
    </div>
  );
}

