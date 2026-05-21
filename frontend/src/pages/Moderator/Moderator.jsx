import { useState } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import ModeratorSidebar from '../../components/Moderator/ModeratorSidebar';
import ModeratorArts from '../../components/Moderator/ModeratorArts';
import ModeratorReports from '../../components/Moderator/ModeratorReports';
import styles from './Moderator.module.css';

function Moderator() {
    const { user } = useAuth();
    const [activeTab, setActiveTab] = useState('arts');
    
    if (!user || (user.userRole !== 'MODERATOR' && user.userRole !== 'ADMIN')) {
        return (
            <div className={styles.accessDenied}>
                <div className={styles.accessDeniedContent}>
                    <h2>Доступ запрещён</h2>
                </div>
            </div>
        );
    }
    
    const isAdmin = user.userRole === 'ADMIN';
    
    return (
        <div className={styles.moderatorPage}>
            <ModeratorSidebar activeTab={activeTab} onTabChange={setActiveTab} isAdmin={isAdmin} />
            <div className={styles.content}>
                {activeTab === 'arts' && <ModeratorArts isAdmin={isAdmin} />}
                {activeTab === 'reports' && <ModeratorReports isAdmin={isAdmin} />}
            </div>
        </div>
    );
}

export default Moderator;