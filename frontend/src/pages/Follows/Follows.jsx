// src/pages/Follows/Follows.jsx
import { useState, useEffect, useCallback } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { followApi } from '../../api/followApi';
import { userApi } from '../../api/userApi';
import Switcher from '../../components/Switcher/Switcher';
import UserCard from '../../components/UserCard/UserCard'; // допустим, у тебя есть такой
import styles from './Follows.module.css';

export default function Follows() {
    const { userId: profileUserId } = useParams(); // чей профиль смотрим
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { user: currentUser, isAuthenticated, isAuthChecked } = useAuth();
    
    const [activeTab, setActiveTab] = useState('subscriptions');
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);

    // 🔥 Читаем tab из URL при монтировании и при изменении searchParams
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

    // Конфиг для свитчера
    const followTabs = [
        { id: 'subscriptions', label: 'Подписки' },
        { id: 'subscribers', label: 'Подписчики' }
    ];

    const loadFollows = useCallback(async (pageNum = 0, reset = false) => {
        if (!isAuthenticated || !currentUser?.id) {
            console.log('[Follows] Не авторизован или нет currentUser');
            return};
        
        console.log('[Follows] Загрузка данных...', {
            activeTab,
            profileUserId,
            currentUserId: currentUser.id,
            pageNum,
            reset
        });

        setLoading(true);
        try {
            const targetUserId = profileUserId ? Number(profileUserId) : currentUser.id;
            const size = 20;
            
            let data;
            if (activeTab === 'subscribers') {
                console.log('[Follows] Запрос подписчиков...');
                
                data = profileUserId 
                    ? await followApi.getFollowers(targetUserId, pageNum, size)
                    : await followApi.getMyFollowers(pageNum, size);
            } else {
                console.log('[Follows] Запрос подписок...');
                
                data = profileUserId 
                    ? await followApi.getFollowing(targetUserId, pageNum, size)
                    : await followApi.getMyFollowing(pageNum, size);
            }
            
            console.log('[Follows] Ответ от API:', data);
            console.log('[Follows] data.content:', data?.content);
            console.log('[Follows] data.content length:', data?.content?.length);

            const userList = followApi.extractUsersFromPage(data, activeTab === 'subscribers' ? 'follower' : 'following');
            console.log('[Follows] Извлеченные пользователи:', userList);
            console.log('[Follows] userList length:', userList.length);


            if (reset || pageNum === 0) {
                setUsers(userList);
            } else {
                setUsers(prev => [...prev, ...userList]);
            }
            
            setHasMore(data?.content?.length === size);
            setPage(pageNum);
            
        } catch (err) {
            console.error('[Follows] Ошибка загрузки подписок:', err);
            console.error('[Follows] Full error:', err)
            console.error('Ошибка загрузки подписок:', err);
        } finally {
            setLoading(false);
        }
    }, [activeTab, profileUserId, currentUser, isAuthenticated]);

    // Перезагружаем список при смене таба или userId
    useEffect(() => {
        if (isAuthenticated && currentUser) {
            loadFollows(0, true);
        }
    }, [activeTab, profileUserId, isAuthenticated, currentUser, loadFollows]);

    const handleTabChange = (tabId) => {
        const newParams = new URLSearchParams(searchParams);
        newParams.set('tab', tabId);
        navigate({ search: newParams.toString() }, { replace: true });
        setActiveTab(tabId);
    };

    const handleLoadMore = () => {
        if (!loading && hasMore) {
            loadFollows(page + 1, false);
        }
    };

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
            
            <div className={styles.list}>
                {users.map(user => (
                    <UserCard 
                        key={user.id}
                        user={user} 
                        showUnfollow={activeTab === 'subscriptions'}
                        onUnfollow={(unfollowedId) => {
                            setUsers(prev => prev.filter(u => u.id !== unfollowedId));
                        }}
                    />
                ))}
            </div>
            
            {loading && page === 0 && <div className={styles.loading}>Загрузка...</div>}

            {!loading && users.length === 0 && (
                <div className={styles.empty}>
                    {activeTab === 'subscribers' 
                        ? 'Пока никто не подписан' 
                        : 'Пока нет подписок'}
                </div>
            )}

            {hasMore && !loading && (
                <button onClick={handleLoadMore} className={styles.loadMore}>
                    Показать ещё
                </button>
            )}
        </div>
    );
}