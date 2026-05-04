// Follows.jsx (фрагменты с ключевыми изменениями)
import { useState, useEffect, useCallback } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { followApi } from '../../api/followApi';
import { searchApi } from '../../api/searchApi';

import SearchBar from '../../components/SearchBar/SearchBar';
import Switcher from '../../components/Switcher/Switcher';
import UserCard from '../../components/UserCard/UserCard';
import styles from './Follows.module.css';

export default function Follows() {
    const { userId: profileUserId } = useParams();
    const [searchParams] = useSearchParams();
    const [userSearchQuery, setUserSearchQuery] = useState(''); // чистый ник, без @
    const navigate = useNavigate();
    const { user: currentUser, isAuthenticated, isAuthChecked } = useAuth();
    
    const [activeTab, setActiveTab] = useState('subscriptions');
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);

    useEffect(() => {
        const tabFromUrl = searchParams.get('tab');
        if (tabFromUrl === 'subscribers' || tabFromUrl === 'subscriptions') {
            setActiveTab(tabFromUrl);
        }
    }, [searchParams]);

    useEffect(() => {
        if (isAuthChecked && !isAuthenticated) {
            navigate('/login', { replace: true });
        }
    }, [isAuthChecked, isAuthenticated, navigate]);

    const followTabs = [
        { id: 'subscriptions', label: 'Подписки' },
        { id: 'subscribers', label: 'Подписчики' }
    ];

    const loadFollows = useCallback(async (pageNum = 0, reset = false, usernameFilter = null) => {
        if (!isAuthenticated || !currentUser?.id) return;
        
        setLoading(true);
        try {
            const targetUserId = profileUserId ? Number(profileUserId) : currentUser.id;
            const size = 20;
            
            let data;
            if (usernameFilter?.trim()) {
                const searchResult = await searchApi.searchUsers(usernameFilter.trim(), pageNum, size);
                const userList = searchResult.content || [];
                
                const usersWithStatus = await Promise.all(
                    userList.map(async (user) => {
                        if (user.id === currentUser?.id) {
                            return { ...user, isSubscribed: false };
                        }
                        try {
                            const isSubscribed = await followApi.isFollowing(user.id);
                            return { ...user, isSubscribed };
                        } catch {
                            return { ...user, isSubscribed: false };
                        }
                    })
                );
                
                if (reset || pageNum === 0) {
                    setUsers(usersWithStatus);
                } else {
                    setUsers(prev => {
                        const existingIds = new Set(prev.map(u => u.id));
                        const newUsers = usersWithStatus.filter(u => !existingIds.has(u.id));
                        return [...prev, ...newUsers];
                    });
                }
                setHasMore(searchResult.content?.length === size);
                setPage(pageNum);
                return;
            }
            
            if (activeTab === 'subscribers') {
                data = profileUserId 
                    ? await followApi.getFollowers(targetUserId, pageNum, size)
                    : await followApi.getMyFollowers(pageNum, size);
            } else {
                data = profileUserId 
                    ? await followApi.getFollowing(targetUserId, pageNum, size)
                    : await followApi.getMyFollowing(pageNum, size);
            }
            
            const userList = followApi.extractUsersFromPage(data, activeTab === 'subscribers' ? 'follower' : 'following');
            
            const usersWithStatus = await Promise.all(
                userList.map(async (user) => {
                    if (user.id === currentUser?.id) {
                        return { ...user, isSubscribed: false };
                    }
                    try {
                        const isSubscribed = await followApi.isFollowing(user.id);
                        return { ...user, isSubscribed };
                    } catch {
                        return { ...user, isSubscribed: false };
                    }
                })
            );
            
            if (reset || pageNum === 0) {
                setUsers(usersWithStatus);
            } else {
                setUsers(prev => {
                    const existingIds = new Set(prev.map(u => u.id));
                    const newUsers = usersWithStatus.filter(u => !existingIds.has(u.id));
                    return [...prev, ...newUsers];
                });
            }
            
            setHasMore(data?.content?.length === size);
            setPage(pageNum);
            
        } catch (err) {
            console.error('[Follows] Ошибка:', err);
        } finally {
            setLoading(false);
        }
    }, [activeTab, profileUserId, currentUser, isAuthenticated]);

    useEffect(() => {
        if (isAuthenticated && currentUser) {
            loadFollows(0, true, userSearchQuery);
        }
    }, [activeTab, profileUserId, isAuthenticated, currentUser, userSearchQuery, loadFollows]);

    const handleToggleSubscribe = useCallback((userId, newIsSubscribed) => {
        setUsers(prev => prev.map(user => 
            user.id === userId ? { ...user, isSubscribed: newIsSubscribed } : user
        ));
    }, []);

    const handleTabChange = (tabId) => {
        const newParams = new URLSearchParams(searchParams);
        newParams.set('tab', tabId);
        navigate({ search: newParams.toString() }, { replace: true });
        setActiveTab(tabId);
    };

    const handleLoadMore = () => {
        if (!loading && hasMore) {
            loadFollows(page + 1, false, userSearchQuery);
        }
    };

    const handleUserSearch = useCallback((query) => {
        const clean = (query || '').replace(/^[@#]/, '').trim();
        setUserSearchQuery(clean);
        setPage(0);
    }, []);

    if (!isAuthChecked) {
        return <div className={styles.loading}>Загрузка...</div>;
    }

    if (!isAuthenticated) {
        return null;
    }

    return (
        <div className={styles.container}>
            <Switcher 
                tabs={followTabs} 
                activeTab={activeTab} 
                onTabChange={handleTabChange} 
            />
            
            <SearchBar
                searchType="username"  
                onSearch={handleUserSearch}
                placeholder="Поиск по никнейму, например: @user или user"
                className={styles.userSearch}
                initialValue={userSearchQuery ? `@${userSearchQuery}` : ''}
            />            

            <div className={styles.list}>
                {users.map(user => (
                    <UserCard 
                        key={user.id}
                        user={user} 
                        isSubscribed={user.isSubscribed}
                        onToggleSubscribe={handleToggleSubscribe} 
                    />
                ))}
            </div>
            
            {loading && page === 0 && <div className={styles.loading}>Загрузка...</div>}

            {!loading && users.length === 0 && (
                <div className={styles.empty}>
                    {userSearchQuery 
                        ? `Пользователь под ником "${userSearchQuery}" не найден`
                        : activeTab === 'subscribers' 
                            ? 'Пока никто не подписан' 
                            : 'Пока нет подписок'}
                </div>
            )}

            {hasMore && !loading && users.length > 0 && (
                <button onClick={handleLoadMore} className={styles.loadMore}>
                    Показать ещё
                </button>
            )}
        </div>
    );
}