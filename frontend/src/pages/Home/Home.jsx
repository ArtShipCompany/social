import { useState, useEffect, useCallback, useRef } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import styles from './Home.module.css';
import { TEXTS } from '../../assets/texts';
import { artApi } from '../../api/artApi';
import { searchApi } from '../../api/searchApi';
import SearchBar from '../../components/SearchBar/SearchBar';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import BoardCard from '../../components/BoardCard/BoardCard';
import ArtCard from '../../components/ArtCard/ArtCard';
import Switcher from '../../components/Switcher/Switcher';
import PFP from '../../assets/WA.jpg';

export default function Home() {
    const { isAuthenticated, isLoading: authLoading, isAuthChecked } = useAuth();
    const [arts, setArts] = useState([]);
    const [searchQuery, setSearchQuery] = useState(''); // только запрос, без ввода
    const [page, setPage] = useState(0);
    const [loading, setLoading] = useState(false);
    const [initialLoading, setInitialLoading] = useState(true);
    const [hasMore, setHasMore] = useState(true);
    const [error, setError] = useState(null);
    const [activeTab, setActiveTab] = useState('recommendations');
    
    const isLoadingRef = useRef(false);

    const feedTabs = [
        { id: 'recommendations', label: 'Рекомендации' },
        { id: 'subscriptions', label: 'Подписки' }
    ];

    const getImageUrl = useCallback((imagePath) => {
        return artApi.utils?.getImageUrl?.(imagePath) || PFP;
    }, []);

    const loadArts = useCallback(async (pageNum = 0, reset = false) => {
        if (isLoadingRef.current) return;
        isLoadingRef.current = true;
        
        if (reset || pageNum === 0) {
            setInitialLoading(true);
        } else {
            setLoading(true);
        }
        
        try {
            if (!isAuthChecked || authLoading) return;
            
            const token = localStorage.getItem('accessToken');
            const isUserAuthenticated = token && isAuthenticated;
            let pageData;
            
            // Логика загрузки в зависимости от режима
            if (searchQuery.trim()) {
                // Поиск по тегу (бэк понимает #теги в smartSearch)
                const tag = searchQuery.replace(/^#/, '');
                pageData = await searchApi.getByTag(tag, pageNum, 30);
            } else if (isUserAuthenticated && activeTab === 'subscriptions') {
                try {
                    pageData = await artApi.getFeedArts(pageNum, 30);
                } catch (feedError) {
                    if (feedError.message?.includes('401') || feedError.message?.includes('Unauthorized')) {
                        setActiveTab('recommendations');
                        pageData = await artApi.getPublicArts(pageNum, 30);
                    } else {
                        throw feedError;
                    }
                }
            } else {
                pageData = await artApi.getPublicArts(pageNum, 30);
            }
            
            // Нормализация ответа (поддерживаем Page и массив)
            let artsData = [];
            let isLast = false;
            
            if (Array.isArray(pageData)) {
                artsData = pageData;
                setHasMore(artsData.length >= 30);
            } else if (pageData?.content) {
                artsData = pageData.content;
                isLast = pageData.last ?? (artsData.length < 30);
                setHasMore(!isLast && artsData.length > 0);
            } else if (pageData?.arts) {
                // Для случаев когда searchApi возвращает { arts, users, ... }
                artsData = pageData.arts;
                setHasMore(pageData.arts?.length >= 30);
            }
            
            if (reset || pageNum === 0) {
                setArts(artsData);
                setPage(0);
            } else {
                setArts(prev => {
                    // Дедупликация по id
                    const existingIds = new Set(prev.map(a => a.id));
                    const newArts = artsData.filter(a => !existingIds.has(a.id));
                    return [...prev, ...newArts];
                });
                setPage(pageNum);
            }
            
            setError(null);
            
        } catch (err) {
            console.error('Error loading arts:', err);
            setError(err.message || 'Ошибка загрузки данных');
            if (reset || pageNum === 0) setArts([]);
        } finally {
            if (reset || pageNum === 0) {
                setInitialLoading(false);
            } else {
                setLoading(false);
            }
            isLoadingRef.current = false;
        }
    }, [isAuthenticated, searchQuery, authLoading, isAuthChecked, activeTab]);

    // 🔁 Перезагрузка при смене запроса/таба
    useEffect(() => {
        if (!isAuthChecked || authLoading) return;
        loadArts(0, true);
    }, [searchQuery, activeTab, loadArts, isAuthChecked, authLoading]);

    const handleTabChange = (tabId) => setActiveTab(tabId);

    // бработчик поиска — принимает уже форматированный запрос от SearchBar
    const handleSearch = useCallback((query) => {
        setSearchQuery(query || '');
        setPage(0);
    }, []);

    const handleLoadMore = () => {
        if (!loading && hasMore) {
            loadArts(page + 1, false);
        }
    };

    const isValidArt = (art) => {
        const imageValue = art?.image || art?.imageUrl;
        return art?.id && imageValue && imageValue !== 'string';
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
                            author={art.author?.username || art.author?.displayName || 'Автор'}
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
                <span className={styles.text}>{TEXTS.boards.private}</span>
                <div className={styles.cardsContainer}>
                    <BoardCard isPrivate={true} />
                    <BoardCard isPrivate={false} />
                </div>
                <span className={styles.text}>{TEXTS.boards.public}</span>
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

            <SearchBar 
                searchType="tag"
                onSearch={handleSearch}
                placeholder="Поиск по тегу, например: #duo"
                initialValue={searchQuery}
            />
            
            {renderContent()}
            
            {hasMore && arts.length > 0 && !loading && (
                <DefaultBtn text={'Показать ещё'} onClick={handleLoadMore} />
            )}
        </>
    );
}