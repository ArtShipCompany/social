import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import styles from './AdminUserTable.module.css';

function AdminUserTable({ 
    users, 
    loading, 
    updatingRole, 
    errorUser,
    sortBy, 
    sortDir, 
    onSort, 
    onRoleChange,
    getRoleName 
}) {
    
    const navigate = useNavigate();
    const { user: currentUser } = useAuth();
    
    const [openSelectId, setOpenSelectId] = useState(null);
    
    const getRoleBadgeClass = (role) => {
        switch(role) {
            case 'ADMIN': return styles.badgeAdmin;
            case 'MODERATOR': return styles.badgeModerator;
            default: return styles.badgeUser;
        }
    };
    
    const formatDate = (dateString) => {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleDateString('ru-RU', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        });
    };
    
    const getRoleLabel = (role) => {
        switch(role) {
            case 'ADMIN': return 'Администратор';
            case 'MODERATOR': return 'Модератор';
            default: return 'Пользователь';
        }
    };
    
    const handleRoleSelect = (userId, newRole) => {
        onRoleChange(userId, newRole);
        setOpenSelectId(null);
    };
    
    const handleUserClick = (userId) => {
        if (currentUser?.id === userId) {
            navigate('/me');
        } else {
            navigate(`/profile/${userId}`);
        }
    };
    
    if (loading && users.length === 0) {
        return (
            <div className={styles.loading}>
                <div className={styles.spinner}></div>
                <p>Загрузка пользователей...</p>
            </div>
        );
    }
    
    if (users.length === 0) {
        return (
            <div className={styles.emptyState}>
                <p>Пользователи не найдены</p>
            </div>
        );
    }
    
    return (
        <div className={styles.tableWrapper}>
            <table className={styles.userTable}>
                <thead>
                    <tr>
                        <th onClick={() => onSort('id')}>
                            ID {sortBy === 'id' && (sortDir === 'asc' ? '↑' : '↓')}
                        </th>
                        <th onClick={() => onSort('username')}>
                            Никнейм {sortBy === 'username' && (sortDir === 'asc' ? '↑' : '↓')}
                        </th>
                        <th>Почта</th>
                        <th>Имя</th>
                        <th onClick={() => onSort('createdAt')}>
                            Дата регистрации {sortBy === 'createdAt' && (sortDir === 'asc' ? '↑' : '↓')}
                        </th>
                        <th>Роль</th>
                        <th>Действия</th>
                    </tr>
                </thead>
                <tbody>
                    {users.map((user, index) => {
                        const isLastRows = index >= users.length - 2;
                        
                        return (
                            <tr 
                                key={user.id} 
                                className={`${styles.clickableRow} ${errorUser === user.id ? styles.errorRow : ''}`}
                            >
                                <td>{user.id}</td>
                                
                                <td 
                                    className={`${styles.username} ${styles.clickableCell}`}
                                    onClick={() => handleUserClick(user.id)}
                                >
                                    {user.username}
                                </td>
                                <td 
                                    className={styles.clickableCell}
                                    onClick={() => handleUserClick(user.id)}
                                >
                                    {user.email || '-'}
                                </td>
                                <td 
                                    className={styles.clickableCell}
                                    onClick={() => handleUserClick(user.id)}
                                >
                                    {user.displayName || '-'}
                                </td>
                                
                                <td>{formatDate(user.createdAt)}</td>
                                <td>
                                    <span className={`${styles.badge} ${getRoleBadgeClass(user.userRole)}`}>
                                        {getRoleName(user.userRole)}
                                    </span>
                                </td>
                                
                                {/* ✅ actionCell с stopPropagation чтобы не триггерить клик по строке */}
                                <td className={styles.actionCell} onClick={(e) => e.stopPropagation()}>
                                    <div className={styles.selectContainer}>
                                        <button
                                            onClick={() => setOpenSelectId(openSelectId === user.id ? null : user.id)}
                                            disabled={updatingRole === user.id}
                                            className={`${styles.selectButton} ${errorUser === user.id ? styles.errorSelect : ''}`}
                                        >
                                            <span className={styles.selectValue}>{getRoleLabel(user.userRole)}</span>
                                            <span className={`${styles.selectArrow} ${openSelectId === user.id ? styles.arrowUp : ''}`}>
                                                ▼
                                            </span>
                                        </button>
                                        
                                        {openSelectId === user.id && (
                                            <div className={`${styles.selectDropdown} ${isLastRows ? styles.dropdownUp : ''}`}>
                                                <div 
                                                    className={`${styles.dropdownOption} ${user.userRole === 'USER' ? styles.dropdownOptionActive : ''}`}
                                                    onClick={() => handleRoleSelect(user.id, 'USER')}
                                                >
                                                    <span>Пользователь</span>
                                                </div>
                                                <div 
                                                    className={`${styles.dropdownOption} ${user.userRole === 'MODERATOR' ? styles.dropdownOptionActive : ''}`}
                                                    onClick={() => handleRoleSelect(user.id, 'MODERATOR')}
                                                >
                                                    <span>Модератор</span>
                                                </div>
                                                <div 
                                                    className={`${styles.dropdownOption} ${user.userRole === 'ADMIN' ? styles.dropdownOptionActive : ''}`}
                                                    onClick={() => handleRoleSelect(user.id, 'ADMIN')}
                                                >
                                                    <span>Администратор</span>
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                    
                                    {updatingRole === user.id && (
                                        <span className={styles.spinnerSmall}></span>
                                    )}
                                    {errorUser === user.id && (
                                        <span className={styles.errorIcon} title="Ошибка изменения роли">⚠️</span>
                                    )}
                                </td>
                            </tr>
                        );
                    })}
                </tbody>
            </table>
            
            {openSelectId && (
                <div 
                    className={styles.dropdownBackdrop}
                    onClick={() => setOpenSelectId(null)}
                />
            )}
        </div>
    );
}

export default AdminUserTable;