import { useState } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import ModerateArts from '../../components/Moderator/ModeratorArts';
import ChangeUserRole from './ChangeUserRole';
import AdminReports from './AdminReports';
import styles from './Admin.module.css';

function Admin() {
    const { user } = useAuth();
    const [activeTab, setActiveTab] = useState('users');
    
    // Проверяем, является ли пользователь админом
    const isAdmin = user?.userRole === 'ADMIN';
    
    if (!user || user.userRole !== 'ADMIN') {
        return (
            <div className={styles.accessDenied}>
                <h2>Доступ запрещён</h2>
            </div>
        );
    }
    
    return (
        <div className={styles.adminPage}>
            <div className={styles.sidebar}>
                <nav className={styles.nav}>
                    <button
                        onClick={() => setActiveTab('users')}
                        className={`${styles.navBtn} ${activeTab === 'users' ? styles.active : ''}`}
                    >
                        <span className={styles.navText}>Управление пользователями</span>
                    </button>
                    <button
                        onClick={() => setActiveTab('arts')}
                        className={`${styles.navBtn} ${activeTab === 'arts' ? styles.active : ''}`}
                    >
                        <span className={styles.navText}>Модерация артов</span>
                    </button>
                    <button
                        onClick={() => setActiveTab('reports')}
                        className={`${styles.navBtn} ${activeTab === 'reports' ? styles.active : ''}`}
                    >
                        <span className={styles.navText}>Жалобы</span>
                    </button>
                </nav>
            </div>
            
            <div className={styles.content}>
                {activeTab === 'users' && <ChangeUserRole />}
                {activeTab === 'arts' && <ModerateArts isAdmin={isAdmin} />}
                {activeTab === 'reports' && <AdminReports />}
            </div>
        </div>
    );
}

export default Admin;