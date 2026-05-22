import { useState } from 'react';
import styles from './AdminSearch.module.css';

function AdminSearch({ onSearch, onReset, size, onSizeChange }) {
    const [searchInput, setSearchInput] = useState('');
    
    const handleSearch = () => {
        onSearch(searchInput);
    };
    
    const handleReset = () => {
        setSearchInput('');
        onReset();
    };
    
    const handleKeyPress = (e) => {
        if (e.key === 'Enter') {
            handleSearch();
        }
    };
    
    return (
        <div className={styles.searchBar}>
            <input
                type="text"
                placeholder="Поиск по username..."
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                onKeyPress={handleKeyPress}
                className={styles.searchInput}
            />
            <button onClick={handleSearch} className={styles.searchBtn}>
                Поиск
            </button>
            <button onClick={handleReset} className={styles.resetBtn}>
                Сбросить
            </button>
            <div className={styles.perPage}>
                <span>Показывать:</span>
                <select value={size} onChange={(e) => onSizeChange(Number(e.target.value))}>
                    <option value={10}>10</option>
                    <option value={20}>20</option>
                    <option value={50}>50</option>
                    <option value={100}>100</option>
                </select>
            </div>
        </div>
    );
}

export default AdminSearch;