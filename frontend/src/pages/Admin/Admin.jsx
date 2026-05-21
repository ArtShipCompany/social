import { useState } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import ChangeUserRole from './ChangeUserRole';
import AdminReports from './AdminReports';
import styles from './Admin.module.css';

function Admin() {
    const { user } = useAuth();
    const [activeTab, setActiveTab] = useState('users');
    
    if (!user || user.userRole !== 'ADMIN') {
        return (
            <div className={styles.accessDenied}>
                <h2>Доступ запрещён</h2>
            </div>
        );
    }
    
    return (
        <div className={styles.adminPage}>
            {/* Sidebar */}
            <div className={styles.sidebar}>
                <nav className={styles.nav}>
                    <button
                        onClick={() => setActiveTab('users')}
                        className={`${styles.navBtn} ${activeTab === 'users' ? styles.active : ''}`}
                    >
                        <span className={styles.navText}>Управление пользователями</span>
                    </button>
                    <button
                        onClick={() => setActiveTab('reports')}
                        className={`${styles.navBtn} ${activeTab === 'reports' ? styles.active : ''}`}
                    >
                        <span className={styles.navText}>Жалобы</span>
                    </button>
                </nav>
            </div>
            
            {/* Content */}
            <div className={styles.content}>
                {activeTab === 'users' && <ChangeUserRole />}
                {activeTab === 'reports' && <AdminReports />}
            </div>
        </div>
    );
}

export default Admin;