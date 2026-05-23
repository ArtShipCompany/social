import styles from './ArtsFilters.module.css';

function ArtsFilters({ 
    statusFilter, 
    onStatusChange, 
    stats 
}) {
    const filters = [
        { value: 'ACTIVE', label: 'Активные', count: stats?.active || 0 },
        { value: 'HIDDEN', label: 'Скрытые', count: stats?.hidden || 0 },
        { value: 'BANNED', label: 'Забанены', count: stats?.banned || 0 },
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

export default ArtsFilters;