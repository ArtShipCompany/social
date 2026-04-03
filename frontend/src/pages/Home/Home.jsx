import { useState, useEffect, useCallback, useRef } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import styles from './Home.module.css';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import BoardCard from '../../components/BoardCard/BoardCard';
import ArtCard from '../../components/ArtCard/ArtCard';
import Switcher from '../../components/Switcher/Switcher';
import SearchIcon from '../../assets/search-icon.svg';
import PFP from '../../assets/WA.jpg'
import { TEXTS } from '../../assets/texts';
import { artApi } from '../../api/artApi';

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
    const feedTabs = [
        { id: 'recommendations', label: 'Рекомендации' },
        { id: 'subscriptions', label: 'Подписки' }
    ];

    const isLoadingRef = useRef(false);

    const getImageUrl = useCallback((imagePath) => {
        return artApi.utils?.getImageUrl?.(imagePath) || PFP;
    }, []);

    const loadArts = useCallback(async (pageNum = 0, isSearchMode = false, reset = false) => {
        if (isLoadingRef.current) return;
        isLoadingRef.current = true;
        
        if (reset || pageNum === 0) {
            setInitialLoading(true);
        } else {
            setLoading(true);
        }
        
        try {
            if (!isAuthChecked || authLoading) {
                return;
            }
            let data;
            
            const token = localStorage.getItem('accessToken');
            const isUserAuthenticated = token && isAuthenticated;
            
            if (isSearchMode && searchQuery.trim()) {
                data = await artApi.searchByTag(searchQuery, pageNum, 30);
            } else if (isUserAuthenticated && activeTab === 'subscriptions') {
                try {
                    data = await artApi.getFeedArts(pageNum, 30);
                } catch (feedError) {
                    if (feedError.message.includes('401') || feedError.message.includes('Unauthorized')) {
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
        if (!isAuthChecked || authLoading) return;
        const isSearchMode = searchQuery.trim() !== '';
        loadArts(0, isSearchMode, true);
    }, [isAuthenticated, searchQuery, loadArts, isAuthChecked, authLoading]);

    useEffect(() => {
        if (isAuthChecked && !authLoading && !searchQuery.trim()) {
            loadArts(0, false, true);
        }
    }, [activeTab, loadArts, isAuthChecked, authLoading, searchQuery]);

    const handleTabChange = (tabId) => setActiveTab(tabId);

    const handleInputChange = (e) => setSearchInput(e.target.value);

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
                            typeShow="full"
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
            
            {isAuthenticated ? (
                <Switcher 
                    tabs={feedTabs} 
                    activeTab={activeTab} 
                    onTabChange={handleTabChange} 
                />
            ) : (
                <div className={styles.switcherSpacer}></div>
            )}

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