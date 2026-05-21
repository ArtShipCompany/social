import ReportsTableRow from './ReportsTableRow';
import styles from './ReportsTable.module.css';

function ReportsTable({ reports, loading, processing, onResolve, onReject }) {
    if (loading) {
        return (
            <div className={styles.loading}>
                <div className={styles.spinner}></div>
                <p>Загрузка жалоб...</p>
            </div>
        );
    }
    
    if (reports.length === 0) {
        return (
            <div className={styles.emptyState}>
                <h3>Жалобы не найдены</h3>
                <p>Нет жалоб, соответствующих выбранным критериям</p>
            </div>
        );
    }
    
    return (
        <div className={styles.tableWrapper}>
            <table className={styles.reportsTable}>
                <thead>
                    <tr>
                        <th>Тип</th>
                        <th>ID контента</th>
                        <th>Причина</th>
                        <th>Жалобщик</th>
                        <th>Автор</th>
                        <th>Дата</th>
                        <th>Статус</th>
                        <th>Действия</th>
                    </tr>
                </thead>
                <tbody>
                    {reports.map((report) => (
                        <ReportsTableRow
                            key={report.id}
                            report={report}
                            processing={processing === report.id}
                            onResolve={onResolve}
                            onReject={onReject}
                        />
                    ))}
                </tbody>
            </table>
        </div>
    );
}

export default ReportsTable;