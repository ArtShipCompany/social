import styles from './AdminStats.module.css';

function AdminStats({ stats }) {
    return (
        <div>
        <span className={styles.title}>Статистика пользователей: </span>
        <div className={styles.stats}>
            <div className={styles.statCard}>
                <div className={styles.statValue}>{stats.totalUsers}</div>
                <div className={styles.statLabel}>Всего пользователей</div>
            </div>
            <div className={styles.statCard}>
                <div className={styles.statValue}>{stats.adminCount}</div>
                <div className={styles.statLabel}>Администраторов</div>
            </div>
            <div className={styles.statCard}>
                <div className={styles.statValue}>{stats.moderatorCount}</div>
                <div className={styles.statLabel}>Модераторов</div>
            </div>
            <div className={styles.statCard}>
                <div className={styles.statValue}>{stats.userCount}</div>
                <div className={styles.statLabel}>Обычных пользователей</div>
            </div>
        </div>              
        </div>
    );
}

export default AdminStats;
