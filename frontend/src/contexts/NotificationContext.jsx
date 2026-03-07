import { createContext, useContext, useState, useCallback } from 'react';
import styles from './NotificationContext.module.css';

const NotificationContext = createContext(null);

export const useNotification = () => {
    const context = useContext(NotificationContext);
    if (!context) {
        throw new Error('useNotification must be used within NotificationProvider');
    }
    return context;
};

export const NotificationProvider = ({ children }) => {
    const [notifications, setNotifications] = useState([]);

    const addNotification = useCallback((message, type = 'error', duration = 5000) => {
        const id = Date.now() + Math.random();
        
        setNotifications(prev => [...prev, { id, message, type }]);
        
        // Автоматическое удаление через duration
        if (duration > 0) {
            setTimeout(() => {
                removeNotification(id);
            }, duration);
        }
        
        return id;
    }, []);

    const removeNotification = useCallback((id) => {
        setNotifications(prev => prev.filter(notif => notif.id !== id));
    }, []);

    const error = useCallback((message, duration) => 
        addNotification(message, 'error', duration), 
    [addNotification]);

    const success = useCallback((message, duration) => 
        addNotification(message, 'success', duration), 
    [addNotification]);

    const warning = useCallback((message, duration) => 
        addNotification(message, 'warning', duration), 
    [addNotification]);

    const info = useCallback((message, duration) => 
        addNotification(message, 'info', duration), 
    [addNotification]);

    return (
        <NotificationContext.Provider value={{ error, success, warning, info }}>
            {children}
            <div className={styles.notificationsContainer}>
                {notifications.map(notification => (
                    <div 
                        key={notification.id} 
                        className={`${styles.notification} ${styles[notification.type]}`}
                    >
                        <span className={styles.message}>{notification.message}</span>
                        <button 
                            onClick={() => removeNotification(notification.id)}
                            className={styles.closeBtn}
                        >
                            ×
                        </button>
                    </div>
                ))}
            </div>
        </NotificationContext.Provider>
    );
};