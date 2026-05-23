import styles from './ReportsFilters.module.css';

function ReportsFilters({ 
    statusFilter, 
    onStatusChange, 
    stats 
}) {
    const filters = [
        { value: 'PENDING', label: 'Ожидают', count: stats?.pending || 0 },
        { value: 'RESOLVED', label: 'Решены', count: stats?.resolved || 0 },
        { value: 'REJECTED', label: 'Отклонены', count: stats?.rejected || 0 },
        { value: '', label: 'Все', count: stats?.all || 0 },
    ];

    return (
        <div className={styles.filters}>
            <div className={styles.label}>
                <span className={styles.filterLabel}>Статус:</span>
            </div>
            <div className={styles.filterButtons}>
                {filters.map((filter) => (
                    <button
                        key={filter.value}
                        onClick={() => onStatusChange(filter.value)}
                        className={`${styles.filterButton} ${statusFilter === filter.value ? styles.active : ''}`}
                    >
                        <span className={styles.buttonCount}>{filter.count}</span>
                        <span className={styles.buttonLabel}>{filter.label}</span>
                    </button>
                ))}
            </div>
        </div>
    );
}

export default ReportsFilters;