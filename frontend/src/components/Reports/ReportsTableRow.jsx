import { useState } from 'react';
import ArtViewModal from '../ArtViewModal/ArtViewModal';
import styles from './ReportsTableRow.module.css';

function ReportsTableRow({ report, processing, onResolve, onReject }) {
    const [showArtModal, setShowArtModal] = useState(false);
    const [selectedArtId, setSelectedArtId] = useState(null);
    const [showRejectDialog, setShowRejectDialog] = useState(false);
    const [rejectNote, setRejectNote] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [tooltip, setTooltip] = useState({ visible: false, text: '', x: 0, y: 0 });
    
    const getTargetTypeName = (type) => {
        return type === 'ART' ? 'Арт' : 'Коммент';
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
    
    const getPriorityDisplay = (priority) => {
        if (!priority) return '1';
        return priority.toString();
    };
    
    const getPriorityClass = (priority) => {
        if (priority >= 4) return styles.priorityHigh;
        if (priority >= 2) return styles.priorityMedium;
        return styles.priorityLow;
    };
    
    const formatDate = (dateString) => {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleString('ru-RU', {
            day: '2-digit',
            month: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    };
    
    const showTooltip = (e, text) => {
        const rect = e.currentTarget.getBoundingClientRect();
        setTooltip({
            visible: true,
            text: text,
            x: rect.right + 5,
            y: rect.top
        });
    };
    
    const hideTooltip = () => {
        setTooltip({ ...tooltip, visible: false });
    };
    
    const handleArtClick = () => {
        if (report.targetType === 'ART' && report.targetId) {
            setSelectedArtId(report.targetId);
            setShowArtModal(true);
        }
    };
    
    const handleRejectClick = () => {
        setShowRejectDialog(true);
        setRejectNote('');
    };
    
    const handleRejectConfirm = async () => {
        if (!rejectNote.trim()) {
            alert('Пожалуйста, укажите причину отклонения');
            return;
        }
        
        setIsSubmitting(true);
        try {
            await onReject(report.id, rejectNote);
            setShowRejectDialog(false);
            setRejectNote('');
        } catch (error) {
            console.error('Error rejecting report:', error);
        } finally {
            setIsSubmitting(false);
        }
    };
    
    const getContentDisplay = () => {
        if (report.targetType === 'ART') {
            return {
                title: report.artTitle || 'Без названия',
                imageUrl: report.artImageUrl,
                onClick: handleArtClick,
                type: 'art'
            };
        } else {
            return {
                title: 'Комментарий',
                text: report.commentText || 'Комментарий',
                type: 'comment'
            };
        }
    };
    
    const content = getContentDisplay();
    
    return (
        <>
            <tr className={report.status === 'PENDING' ? styles.pendingRow : ''}>
                <td className={styles.targetType}>
                    <span className={styles.targetIcon}>
                        {getTargetTypeName(report.targetType)}
                    </span>
                </td>
                
                <td 
                    className={styles.contentCell}
                    onClick={content.onClick}
                    style={{ cursor: content.type === 'art' ? 'pointer' : 'default' }}
                >
                    <div className={styles.contentText}>
                        <strong 
                        className={styles.contentTitle}
                        onMouseEnter={(e) => showTooltip(e, content.title)}
                        onMouseLeave={hideTooltip}                        
                        >
                            {content.title}
                        </strong>
                        <span className={styles.contentId}>ID: {report.targetId}</span>
                    </div>
                </td>
                
                <td className={styles.reason}>
                    <div className={styles.reasonContent}>
                        <span className={styles.reasonText}>{report.reason}</span>
                    </div>
                    {report.description && (
                        <div 
                        className={styles.reasonDesc}
                        onMouseEnter={(e) => showTooltip(e, report.description || report.reason)}
                        onMouseLeave={hideTooltip}
                        >
                            {report.description}
                        </div>
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
                                className={styles.deleteBtn}
                                title="Удалить контент"
                            >
                                Удалить
                            </button>
                            <button
                                onClick={() => onResolve(report.id, false)}
                                disabled={processing}
                                className={styles.hideBtn}
                                title="Скрыть контент"
                            >
                                Скрыть
                            </button>
                            <button
                                onClick={handleRejectClick}
                                disabled={processing}
                                className={styles.rejectBtn}
                                title="Отклонить жалобу"
                            >
                                Отклонить
                            </button>
                        </div>
                    ) : (
                        <span 
                            className={styles.resolvedInfo}
                        >
                            {report.resolutionNote}
                        </span>
                    )}
                </td>
            </tr>
            
            {/* Кастомная подсказка */}
            {tooltip.visible && (
                <div 
                    className={styles.customTooltip}
                    style={{
                        left: `${tooltip.x}px`,
                        top: `${tooltip.y}px`,
                        transform: 'translateY(-100%) translateX(-30%)',
                    }}
                >
                    <div className={styles.tooltipContent}>
                        {tooltip.text}
                    </div>
                </div>
            )}
            
            {/* Модальное окно для отклонения жалобы */}
            {showRejectDialog && (
                <div className={styles.dialogOverlay} onClick={() => setShowRejectDialog(false)}>
                    <div className={styles.dialog} onClick={(e) => e.stopPropagation()}>
                        <div className={styles.dialogHeader}>
                            <h3>Отклонение жалобы</h3>
                            <button 
                                className={styles.dialogClose} 
                                onClick={() => setShowRejectDialog(false)}
                            >
                                &times;
                            </button>
                        </div>
                        <div className={styles.dialogBody}>
                            <p>Вы уверены, что хотите отклонить эту жалобу?</p>
                            <label className={styles.dialogLabel}>
                                Причина отклонения:
                                <textarea
                                    value={rejectNote}
                                    onChange={(e) => setRejectNote(e.target.value)}
                                    placeholder="Укажите причину отклонения жалобы..."
                                    className={styles.dialogTextarea}
                                    rows={3}
                                />
                            </label>
                        </div>
                        <div className={styles.dialogFooter}>
                            <button 
                                onClick={() => setShowRejectDialog(false)}
                                className={styles.dialogCancelBtn}
                            >
                                Отмена
                            </button>
                            <button 
                                onClick={handleRejectConfirm}
                                disabled={isSubmitting || !rejectNote.trim()}
                                className={styles.dialogConfirmBtn}
                            >
                                {isSubmitting ? 'Отклонение...' : 'Подтвердить'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
            
            {showArtModal && (
                <ArtViewModal
                    artId={selectedArtId}
                    onClose={() => setShowArtModal(false)}
                />
            )}
        </>
    );
}

export default ReportsTableRow;