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
  searchType = 'tag', // 'tag' | 'username' | 'text'
}) {
  const [inputValue, setInputValue] = useState(initialValue);

  const handleInputChange = useCallback((e) => {
    setInputValue(e.target.value);
  }, []);

  const handleKeyDown = useCallback((e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      const query = inputValue.trim();
      
      if (query) {
        let formatted = query;
        
        if (searchType === 'tag' && !query.startsWith('#')) {
          formatted = `#${query}`;
        } else if (searchType === 'username') {
          formatted = query.replace(/^[@#]/, '');
        }
        
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

  // Синхронизация initialValue
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