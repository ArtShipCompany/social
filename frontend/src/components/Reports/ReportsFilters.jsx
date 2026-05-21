import styles from './ReportsFilters.module.css';

function ReportsFilters({ statusFilter, onStatusChange, size, onSizeChange }) {
    return (
        <div className={styles.filters}>
            <div className={styles.filterGroup}>
                <label className={styles.filterLabel}>Статус:</label>
                <select 
                    value={statusFilter} 
                    onChange={(e) => onStatusChange(e.target.value)}
                    className={styles.filterSelect}
                >
                    <option value="">Все</option>
                    <option value="PENDING">Ожидают</option>
                    <option value="RESOLVED">Решены</option>
                    <option value="REJECTED">Отклонены</option>
                </select>
            </div>
            
            <div className={styles.filterGroup}>
                <label className={styles.filterLabel}>Показывать:</label>
                <select 
                    value={size} 
                    onChange={(e) => onSizeChange(Number(e.target.value))}
                    className={styles.filterSelect}
                >
                    <option value={10}>10</option>
                    <option value={20}>20</option>
                    <option value={50}>50</option>
                    <option value={100}>100</option>
                </select>
            </div>
        </div>
    );
}

export default ReportsFilters;