import styles from './ArtsPagination.module.css';

function ArtsPagination({ page, totalPages, totalElements, size, onPageChange }) {
    const startItem = page * size + 1;
    const endItem = Math.min((page + 1) * size, totalElements);
    
    if (totalPages === 0) return null;
    
    return (
        <div className={styles.pagination}>
            <div className={styles.info}>
                Показано <span className={styles.highlight}>{startItem}</span> — <span className={styles.highlight}>{endItem}</span> из <span className={styles.highlight}>{totalElements}</span> артов
            </div>
            <div className={styles.controls}>
                <button
                    onClick={() => onPageChange(page - 1)}
                    disabled={page === 0}
                    className={styles.pageBtn}
                >
                    ← Назад
                </button>
                <span className={styles.pageInfo}>
                    Страница {page + 1} из {totalPages}
                </span>
                <button
                    onClick={() => onPageChange(page + 1)}
                    disabled={page >= totalPages - 1}
                    className={styles.pageBtn}
                >
                    Вперед →
                </button>
            </div>
        </div>
    );
}

export default ArtsPagination;