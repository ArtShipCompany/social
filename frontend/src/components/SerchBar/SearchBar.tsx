import styles from './SearchBar.module.css';

import SearchIcon from '../../assets/search-icon.svg';

export const SearchBar = () => {
  return (
    <div className={styles.searchContainer}>
      <div className={styles.searchInputWrapper}>
        <img src={SearchIcon} alt="Поиск" className={styles.icon} />
        <input
          type="text"
          placeholder="Поиск..."
          className={styles.searchInput}
        />
        
      </div>
    </div>
  );
};

export default SearchBar;