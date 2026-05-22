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
    
    if (loading) {
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
                            Username {sortBy === 'username' && (sortDir === 'asc' ? '↑' : '↓')}
                        </th>
                        <th>Email</th>
                        <th>Display Name</th>
                        <th onClick={() => onSort('createdAt')}>
                            Дата регистрации {sortBy === 'createdAt' && (sortDir === 'asc' ? '↑' : '↓')}
                        </th>
                        <th>Роль</th>
                        <th>Действия</th>
                    </tr>
                </thead>
                <tbody>
                    {users.map((user) => (
                        <tr key={user.id} className={errorUser === user.id ? styles.errorRow : ''}>
                            <td>{user.id}</td>
                            <td className={styles.username}>{user.username}</td>
                            <td>{user.email || '-'}</td>
                            <td>{user.displayName || '-'}</td>
                            <td>{formatDate(user.createdAt)}</td>
                            <td>
                                <span className={`${styles.badge} ${getRoleBadgeClass(user.userRole)}`}>
                                    {getRoleName(user.userRole)}
                                </span>
                            </td>
                            <td>
                                <select
                                    value={user.userRole}
                                    onChange={(e) => onRoleChange(user.id, e.target.value)}
                                    disabled={updatingRole === user.id}
                                    className={`${styles.roleSelect} ${errorUser === user.id ? styles.errorSelect : ''}`}
                                >
                                    <option value="USER">Пользователь</option>
                                    <option value="MODERATOR">Модератор</option>
                                    <option value="ADMIN">Администратор</option>
                                </select>
                                {updatingRole === user.id && (
                                    <span className={styles.spinnerSmall}></span>
                                )}
                                {errorUser === user.id && (
                                    <span className={styles.errorIcon} title="Ошибка изменения роли">⚠️</span>
                                )}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

export default AdminUserTable;