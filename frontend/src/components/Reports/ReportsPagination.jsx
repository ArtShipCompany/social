import styles from './ReportsPagination.module.css';

function ReportsPagination({ page, totalPages, totalElements, size, onPageChange }) {
    const startItem = page * size + 1;
    const endItem = Math.min((page + 1) * size, totalElements);
    
    const getPageNumbers = () => {
        const pages = [];
        const maxVisible = 5;
        
        if (totalPages <= maxVisible) {
            for (let i = 0; i < totalPages; i++) {
                pages.push(i);
            }
        } else {
            if (page <= 2) {
                for (let i = 0; i < maxVisible - 1; i++) {
                    pages.push(i);
                }
                pages.push('...');
                pages.push(totalPages - 1);
            } else if (page >= totalPages - 3) {
                pages.push(0);
                pages.push('...');
                for (let i = totalPages - 4; i < totalPages; i++) {
                    pages.push(i);
                }
            } else {
                pages.push(0);
                pages.push('...');
                for (let i = page - 1; i <= page + 1; i++) {
                    pages.push(i);
                }
                pages.push('...');
                pages.push(totalPages - 1);
            }
        }
        
        return pages;
    };
    
    if (totalPages === 0) return null;
    
    return (
        <div className={styles.pagination}>
            <div className={styles.info}>
                Показано {startItem}-{endItem} из {totalElements} жалоб
            </div>
            <div className={styles.controls}>
                <button
                    onClick={() => onPageChange(page - 1)}
                    disabled={page === 0}
                    className={styles.pageBtn}
                >
                    ← Назад
                </button>
                
                {getPageNumbers().map((p, index) => (
                    p === '...' ? (
                        <span key={`dots-${index}`} className={styles.dots}>...</span>
                    ) : (
                        <button
                            key={p}
                            onClick={() => onPageChange(p)}
                            className={`${styles.pageBtn} ${page === p ? styles.active : ''}`}
                        >
                            {p + 1}
                        </button>
                    )
                ))}
                
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

export default ReportsPagination;