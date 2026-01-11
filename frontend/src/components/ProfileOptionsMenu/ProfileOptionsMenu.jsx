import { useState, useEffect, useRef } from 'react';
import styles from './ProfileOptionsMenu.module.css';
import privacyIcon from '../../assets/private-edit.svg';
import deleteIcon from '../../assets/delete-icon.svg';
import ellipsisIcon from '../../assets/ellipsis-icon.svg';

export default function ProfileOptionsMenu({ 
  isOpen, 
  onToggle, 
  onPrivacyClick, 
  onDeleteClick 
}) {
  const menuRef = useRef(null);

  return (
    <div ref={menuRef} style={{ position: 'relative' }}>
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
          <button
            className={styles.menuItem}
            onClick={onPrivacyClick}
          >
            <img src={privacyIcon} alt="Приватность" className={styles.icon} />
          </button>

          <button
            className={styles.menuItem}
            onClick={onDeleteClick}
          >
            <img src={deleteIcon} alt="Удалить" className={styles.icon} />
          </button>
        </div>
      )}
    </div>
  );
}

