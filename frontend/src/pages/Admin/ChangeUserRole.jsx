import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { useNotification } from '../../contexts/NotificationContext';
import { adminApi } from '../../api/adminApi';
import AdminStats from '../../components/Admin/AdminStats';
import AdminSearch from '../../components/Admin/AdminSearch';
import AdminUserTable from '../../components/Admin/AdminUserTable';
import AdminPagination from '../../components/Admin/AdminPagination';
import styles from './ChangeUserRole.module.css';

function ChangeUserRole() {
    const { user, refreshUser } = useAuth();
    const notification = useNotification();
    
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [updatingRole, setUpdatingRole] = useState(null);
    
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(20);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [sortBy, setSortBy] = useState('createdAt');
    const [sortDir, setSortDir] = useState('desc');
    const [search, setSearch] = useState('');
    const [searchInput, setSearchInput] = useState('');
    
    const [stats, setStats] = useState({
        totalUsers: 0,
        adminCount: 0,
        moderatorCount: 0,
        userCount: 0
    });
    
    const loadUsers = useCallback(async () => {
        try {
            setLoading(true);
            console.log('Loading users with params:', { page, size, sortBy, sortDir, search });
            const response = await adminApi.getAllUsers(page, size, sortBy, sortDir, search);
            console.log('Users loaded:', response);
            setUsers(response.content || []);
            setTotalPages(response.totalPages || 0);
            setTotalElements(response.totalElements || 0);
        } catch (error) {
            console.error('Load users error:', error);
            notification.error('Ошибка загрузки пользователей');
        } finally {
            setLoading(false);
        }
    }, [page, size, sortBy, sortDir, search, notification]);
    
    const loadStatistics = useCallback(async () => {
        try {
            const data = await adminApi.getRoleStatistics();
            console.log('Statistics loaded:', data);
            setStats(data);
        } catch (error) {
            console.error('Error loading statistics:', error);
        }
    }, []);
    
    useEffect(() => {
        loadUsers();
        loadStatistics();
    }, [loadUsers, loadStatistics]);
    
    const handleRoleChange = async (userId, newRole) => {
        console.log('=== CHANGING ROLE ===');
        console.log('User ID:', userId);
        console.log('New role:', newRole);
        console.log('Current user:', user);
        
        setUpdatingRole(userId);
        
        try {
            const updatedUser = await adminApi.changeUserRole(userId, newRole);
            console.log('API response:', updatedUser);
            
            notification.success(`Роль пользователя изменена на ${getRoleName(newRole)}`);
            
            // Обновляем список пользователей
            await loadUsers();
            await loadStatistics();
            
            // Если меняем роль текущего пользователя
            if (user && user.id === userId) {
                console.log('Changing current user role, refreshing...');
                refreshUser();
                
                // Если понизили себя с админа
                if (newRole !== 'ADMIN') {
                    notification.warning('Ваша роль изменена, страница будет перезагружена');
                    setTimeout(() => {
                        window.location.href = '/';
                    }, 2000);
                }
            }
            
        } catch (error) {
            console.error('Role change error:', error);
            notification.error(error.message || 'Ошибка изменения роли');
        } finally {
            setUpdatingRole(null);
        }
    };
    
    const getRoleName = (role) => {
        switch(role) {
            case 'ADMIN': return 'Администратор';
            case 'MODERATOR': return 'Модератор';
            default: return 'Пользователь';
        }
    };
    
    const handleSort = (field) => {
        if (sortBy === field) {
            setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
        } else {
            setSortBy(field);
            setSortDir('desc');
        }
        setPage(0);
    };
    
    const handleSearch = () => {
        setSearch(searchInput);
        setPage(0);
    };
    
    const handleResetSearch = () => {
        setSearchInput('');
        setSearch('');
        setPage(0);
    };
    
    const handleSizeChange = (newSize) => {
        setSize(newSize);
        setPage(0);
    };
    
    
    return (
        <div className={styles.adminPage}>
            <div className={styles.header}>
                <h1>Управление пользователями</h1>
            </div>
            <AdminStats stats={stats} />
            <AdminSearch 
                searchInput={searchInput}
                onSearchInputChange={setSearchInput}
                onSearch={handleSearch}
                onReset={handleResetSearch}
                size={size}
                onSizeChange={handleSizeChange}
            />
            <AdminUserTable 
                users={users}
                loading={loading}
                updatingRole={updatingRole}
                sortBy={sortBy}
                sortDir={sortDir}
                onSort={handleSort}
                onRoleChange={handleRoleChange}
                getRoleName={getRoleName}
                currentUserId={user?.id}
            />
            <AdminPagination 
                page={page}
                totalPages={totalPages}
                totalElements={totalElements}
                size={size}
                onPageChange={setPage}
            />
        </div>
    );
}

export default ChangeUserRole;