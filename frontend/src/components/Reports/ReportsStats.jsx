import styles from './ReportsStats.module.css';

function ReportsStats({ stats }) {
    return (
        <div className={styles.stats}>
            <div className={`${styles.statCard} ${styles.totalCard}`}>
                <div className={styles.statValue}>{stats.total}</div>
                <div className={styles.statLabel}>Всего жалоб</div>
            </div>
            <div className={`${styles.statCard} ${styles.pendingCard}`}>
                <div className={styles.statValue}>{stats.pending}</div>
                <div className={styles.statLabel}>Ожидают</div>
            </div>
            <div className={`${styles.statCard} ${styles.resolvedCard}`}>
                <div className={styles.statValue}>{stats.resolved}</div>
                <div className={styles.statLabel}>Решены</div>
            </div>
            <div className={`${styles.statCard} ${styles.rejectedCard}`}>
                <div className={styles.statValue}>{stats.rejected}</div>
                <div className={styles.statLabel}>Отклонены</div>
            </div>
        </div>
    );
}

export default ReportsStats;