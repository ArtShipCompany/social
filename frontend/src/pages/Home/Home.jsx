import { useState } from 'react';
import styles from './Home.module.css';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import BoardCard from '../../components/BoardCard/BoardCard';
import ArtCard from '../../components/ArtCard/ArtCard';
import { mockArts } from '../../mock-images/mockArts';
import SearchIcon from '../../assets/search-icon.svg';
import { TEXTS } from '../../assets/texts';

export default function Home() {
    const [searchInput, setSearchInput] = useState('');
    const [searchQuery, setSearchQuery] = useState(''); 

    const filteredArts = mockArts.filter(art => {
        if (!searchQuery.trim()) return true;

        const query = searchQuery.trim().toLowerCase();
        const tagsArray = (art.tags || '').toLowerCase().match(/#[^\s#]+/g) || [];
        return tagsArray.some(tag => tag === query);
    });

    const handleInputChange = (e) => {
        setSearchInput(e.target.value);
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            setSearchQuery(searchInput);
        }
    };

    return (
        <>
            <div className={styles.boards}>
                <span className={styles.text}>
                    {TEXTS.boards.private}
                </span>

                <div className={styles.cardsContainer}>
                    <BoardCard isPrivate={true} />
                    <BoardCard isPrivate={false} />
                </div>

                <span className={styles.text}>
                    {TEXTS.boards.public}
                </span>
            </div>
            
            <div className={styles.search}>
                <div className={styles.searchInputWrapper}>
                    <img src={SearchIcon} alt="Поиск" className={styles.icon} />
                    <input
                        type="text"
                        placeholder="Поиск по тегу, например: #duo"
                        className={styles.searchInput}
                        value={searchInput}
                        onChange={handleInputChange}
                        onKeyDown={handleKeyDown}
                    />
                </div>
            </div>

            <div className={styles.feed}>
                {filteredArts.length > 0 ? (
                    filteredArts.map(art => (
                        <ArtCard key={art.id} id={art.id} image={art.image} showLikeButton={true} />
                    ))
                    ) : (
                    <div className={styles.noResults}>Ничего не найдено</div>
                )}
            </div>

            <DefaultBtn text={'Показать ещё'} />

        </>
    );
};
