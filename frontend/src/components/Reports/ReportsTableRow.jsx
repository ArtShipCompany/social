import styles from './ReportsTableRow.module.css';

function ReportsTableRow({ report, processing, onResolve, onReject }) {
    const getTargetTypeName = (type) => {
        return type === 'ART' ? 'Арт' : 'Комментарий';
    };
    
    const getStatusBadgeClass = (status) => {
        switch(status) {
            case 'PENDING': return styles.statusPending;
            case 'RESOLVED': return styles.statusResolved;
            case 'REJECTED': return styles.statusRejected;
            default: return styles.statusPending;
        }
    };
    
    const getStatusName = (status) => {
        switch(status) {
            case 'PENDING': return 'Ожидает';
            case 'RESOLVED': return 'Решена';
            case 'REJECTED': return 'Отклонена';
            default: return status;
        }
    };
    
    const getPriorityIcon = (priority) => {
        if (priority >= 4) return '🔴';
        if (priority >= 2) return '🟡';
        return '🟢';
    };
    
    const formatDate = (dateString) => {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleString('ru-RU', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };
    
    return (
        <tr className={report.status === 'PENDING' ? styles.pendingRow : ''}>
            <td className={styles.targetType}>
                <span className={styles.targetIcon}>
                    {getTargetTypeName(report.targetType)}
                </span>
            </td>
            <td className={styles.targetId}>#{report.targetId}</td>
            <td className={styles.reason}>
                <div className={styles.reasonContent}>
                    <span className={styles.priorityIcon}>{getPriorityIcon(report.priority)}</span>
                    <span>{report.reason}</span>
                </div>
                {report.description && (
                    <div className={styles.reasonDesc}>{report.description}</div>
                )}
            </td>
            <td className={styles.reporter}>
                <span className={styles.userId}>ID: {report.reporterId}</span>
            </td>
            <td className={styles.author}>
                {report.artAuthorUsername || report.commentAuthorUsername || '-'}
            </td>
            <td className={styles.date}>{formatDate(report.createdAt)}</td>
            <td>
                <span className={`${styles.statusBadge} ${getStatusBadgeClass(report.status)}`}>
                    {getStatusName(report.status)}
                </span>
                {report.resolvedBy && (
                    <div className={styles.resolvedBy}>
                        {report.resolvedBy}
                    </div>
                )}
            </td>
            <td className={styles.actions}>
                {report.status === 'PENDING' ? (
                    <div className={styles.actionButtons}>
                        <button
                            onClick={() => onResolve(report.id, true)}
                            disabled={processing}
                            className={styles.resolveBtn}
                            title="Подтвердить жалобу и удалить контент"
                        >
                            Удалить
                        </button>
                        <button
                            onClick={() => onResolve(report.id, false)}
                            disabled={processing}
                            className={styles.hideBtn}
                            title="Подтвердить жалобу, но оставить контент"
                        >
                            Скрыть
                        </button>
                        <button
                            onClick={() => onReject(report.id)}
                            disabled={processing}
                            className={styles.rejectBtn}
                            title="Отклонить жалобу"
                        >
                            Отклонить
                        </button>
                    </div>
                ) : (
                    <span className={styles.resolvedInfo}>
                        {report.resolutionNote}
                    </span>
                )}
            </td>
        </tr>
    );
}

export default ReportsTableRow;