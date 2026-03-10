import { useState, useEffect, useCallback, useRef } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import styles from './Home.module.css';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import BoardCard from '../../components/BoardCard/BoardCard';
import ArtCard from '../../components/ArtCard/ArtCard';
import SearchIcon from '../../assets/search-icon.svg';
import PFP from '../../assets/WA.jpg'
import { TEXTS } from '../../assets/texts';
import artApi from '../../api/artApi';

export default function Home() {
    const { isAuthenticated, isLoading: authLoading, isAuthChecked } = useAuth();
    const [arts, setArts] = useState([]);
    const [searchInput, setSearchInput] = useState('');
    const [searchQuery, setSearchQuery] = useState('');
    const [page, setPage] = useState(0);
    const [loading, setLoading] = useState(false);
    const [initialLoading, setInitialLoading] = useState(true);
    const [hasMore, setHasMore] = useState(true);
    const [error, setError] = useState(null);

    const [activeTab, setActiveTab] = useState('recommendations');
    
    const isLoadingRef = useRef(false);

    const getImageUrl = (imagePath) => {
        if (!imagePath) return PFP;
        
        if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
            return imagePath;
        }
        
        let finalPath = imagePath;
        
        if (imagePath.startsWith('/api/files/images/')) {
            const filename = imagePath.split('/').pop();
            finalPath = `/uploads/images/${filename}`;
        }
        else if (imagePath.startsWith('/uploads/')) {
            finalPath = imagePath;
        }
        else if (imagePath.startsWith('uploads/')) {
            finalPath = `/${imagePath}`;
        }
        else if (!imagePath.includes('/')) {
            finalPath = `/uploads/images/${imagePath}`;
        }
        
        return `http://localhost:8081${finalPath}`;
    };

    const loadArts = useCallback(async (pageNum = 0, isSearchMode = false, reset = false) => {
        if (isLoadingRef.current) return;
        
        isLoadingRef.current = true;
        
        if (reset || pageNum === 0) {
            setInitialLoading(true);
        } else {
            setLoading(true);
        }
        
        try {
            let data;

            if (!isAuthChecked || authLoading) {
                console.log('Waiting for auth check to complete...');
                return;
            }
            
            const token = localStorage.getItem('accessToken');
            const isUserAuthenticated = token && isAuthenticated;
            
            if (isSearchMode && searchQuery.trim()) {
                data = await artApi.searchByTag(searchQuery, pageNum, 30);
            } else if (isUserAuthenticated && activeTab === 'subscriptions') {
                console.log('Loading feed (subscriptions)...');
                try {
                    data = await artApi.getFeedArts(pageNum, 30);
                } catch (feedError) {
                    console.error('Feed load error:', feedError);
                    if (feedError.message.includes('401') || feedError.message.includes('Unauthorized')) {
                        console.log('Token invalid, falling back to public arts');
                        setActiveTab('recommendations');
                        data = await artApi.getPublicArts(pageNum, 30);
                    } else {
                        throw feedError;
                    }
                }
            } else {
                data = await artApi.getPublicArts(pageNum, 30);
            }
            
            let artsData = [];
            let isLast = false;
            
            if (Array.isArray(data)) {
                artsData = data;
                setHasMore(artsData.length === 30);
            } else if (data && data.content) {
                artsData = data.content || [];
                isLast = data.last || false;
                setHasMore(!isLast && artsData.length > 0);
            }
            
            if (reset || pageNum === 0) {
                setArts(artsData);
                setPage(0);
            } else {
                setArts(prev => [...prev, ...artsData]);
                setPage(pageNum);
            }
            
            setError(null);
            
        } catch (err) {
            console.error('Error loading arts:', err);
            setError(err.message || 'Ошибка загрузки данных');
            
            if (reset || pageNum === 0) {
                setArts([]);
            }
        } finally {
            if (reset || pageNum === 0) {
                setInitialLoading(false);
            } else {
                setLoading(false);
            }
            isLoadingRef.current = false;
        }
    }, [isAuthenticated, searchQuery, authLoading, isAuthChecked, activeTab]);

    useEffect(() => {
        if (!isAuthChecked || authLoading) {
            return;
        }
        
        const isSearchMode = searchQuery.trim() !== '';
        loadArts(0, isSearchMode, true);
    }, [isAuthenticated, searchQuery, loadArts, isAuthChecked, authLoading]);

    useEffect(() => {
        if (isAuthChecked && !authLoading && !searchQuery.trim()) {
            loadArts(0, false, true);
        }
    }, [activeTab]);

    const handleTabChange = (tab) => {
        if (activeTab !== tab) {
            setActiveTab(tab);
        }
    };

    const handleInputChange = (e) => {
        setSearchInput(e.target.value);
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            const query = searchInput.trim();
            if (query) {
                const formattedQuery = query.startsWith('#') ? query : `#${query}`;
                setSearchQuery(formattedQuery);
            } else {
                setSearchQuery('');
            }
        }
    };

    const handleClearSearch = () => {
        setSearchInput('');
        setSearchQuery('');
    };

    const handleLoadMore = () => {
        const isSearchMode = searchQuery.trim() !== '';
        loadArts(page + 1, isSearchMode, false);
    };

    const isValidArt = (art) => {
        const imageValue = art.image || art.imageUrl;
        return art && art.id && imageValue && imageValue !== 'string';
    };

    const renderContent = () => {
        if (!isAuthChecked || authLoading || initialLoading) {
            return <div className={styles.loading}>Загрузка...</div>;
        }
        
        if (error && arts.length === 0) {
            return <div className={styles.error}>Ошибка: {error}</div>;
        }
        
        const validArts = arts.filter(isValidArt);
        
        if (validArts.length === 0) {
            return (
                <div className={styles.noResults}>
                    {searchQuery 
                        ? `Ничего не найдено по тегу ${searchQuery}`
                        : isAuthenticated
                            ? 'Ваша лента подписок пуста. Подпишитесь на авторов!'
                            : 'Нет публичных артов для отображения'
                    }
                </div>
            );
        }
        
        return (
            <div className={styles.feed}>
                {validArts.map(art => {
                    const imageUrl = getImageUrl(art.image);
                    
                    return (
                        <ArtCard 
                            key={art.id} 
                            id={art.id} 
                            image={imageUrl}
                            typeShow="showLikes"
                            likesCount={art.likesCount || 0}
                            initialIsPrivate={art.isPublicFlag === false}
                            title={art.title || 'Без названия'}
                            author={art.author || art.username || 'Автор'}
                        />
                    );
                })}
                
                {loading && page > 0 && (
                    <div className={styles.loadingMore}>Загрузка...</div>
                )}
            </div>
        );
    };


    if (!isAuthChecked || authLoading) {
        return <div className={styles.loading}>Проверка авторизации...</div>;
    }

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
                {isAuthenticated ? (
                    <div className={styles.switcher}>
                        <button
                            className={`${styles.switcherTab} ${activeTab === 'recommendations' ? styles.active : ''}`}
                            onClick={() => handleTabChange('recommendations')}
                        >
                            Рекомендации
                        </button>
                        <button
                            className={`${styles.switcherTab} ${activeTab === 'subscriptions' ? styles.active : ''}`}
                            onClick={() => handleTabChange('subscriptions')}
                        >
                            Подписки
                        </button>
                    </div>
                ) : (
                    <div className={styles.switcherSpacer}></div>
                )}
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
                    {(searchInput || searchQuery) && (
                        <button 
                            onClick={handleClearSearch}
                            className={styles.clearButton}
                            title="Очистить поиск"
                        >
                            ×
                        </button>
                    )}
                </div>
            </div>

            
                {renderContent()}
            
            
            {hasMore && arts.length > 0 && !loading && (
                <DefaultBtn text={'Показать ещё'} onClick={handleLoadMore} />
            )}
        </>
    );
};