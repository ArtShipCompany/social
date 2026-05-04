import { useState, useCallback, useEffect } from 'react';
import styles from './SearchBar.module.css';
import SearchIcon from '../../assets/search-icon.svg';

export default function SearchBar({ 
  onSearch, 
  placeholder = 'Поиск по тегу, например: #duo',
  initialValue = '',
  className = '',
  autoFocus = false,
  disabled = false,
  searchType = 'tag', // 'tag' | 'username' | 'all'
}) {
  const [inputValue, setInputValue] = useState(initialValue);

  const handleInputChange = useCallback((e) => {
    setInputValue(e.target.value);
  }, []);

  const handleKeyDown = useCallback((e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      const raw = inputValue.trim();
      
      if (raw) {
        let formatted = raw;
        
        if (searchType === 'tag') {
          // Для тегов: добавляем # если нет, но принимаем и без
          formatted = raw.startsWith('#') ? raw : `#${raw}`;
        } else if (searchType === 'username') {
          // Для юзеров: убираем @ и #, бэк сам обработает
          formatted = raw.replace(/^[@#]/, '');
        }
        // searchType === 'all' — отправляем как есть, бэк умный
        
        onSearch?.(formatted);
      } else {
        onSearch?.('');
      }
    }
  }, [inputValue, onSearch, searchType]);

  const handleClear = useCallback(() => {
    setInputValue('');
    onSearch?.('');
  }, [onSearch]);

  useEffect(() => {
    setInputValue(initialValue);
  }, [initialValue]);

  return (
    <div className={`${styles.search} ${className}`}>
      <div className={styles.searchInputWrapper}>
        <img src={SearchIcon} alt="Поиск" className={styles.icon} />
        <input
          type="text"
          placeholder={placeholder}
          className={styles.searchInput}
          value={inputValue}
          onChange={handleInputChange}
          onKeyDown={handleKeyDown}
          disabled={disabled}
          autoFocus={autoFocus}
          aria-label="Поиск"
        />
        {inputValue && (
          <button 
            type="button"
            onClick={handleClear}
            className={styles.clearButton}
            title="Очистить поиск"
            aria-label="Очистить поиск"
          >
            ×
          </button>
        )}
      </div>
    </div>
  );
}