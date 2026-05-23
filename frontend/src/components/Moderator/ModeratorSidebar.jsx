import styles from './ModeratorSidebar.module.css';

function ModeratorSidebar({ activeTab, onTabChange, isAdmin }) {
    return (
        <div className={styles.sidebar}>
           
            <nav className={styles.nav}>
                <button
                    onClick={() => onTabChange('arts')}
                    className={`${styles.navBtn} ${activeTab === 'arts' ? styles.active : ''}`}
                >
                    <span className={styles.navText}>Модерация артов</span>
                    {activeTab === 'arts' && <span className={styles.activeIndicator} />}
                </button>
                
                <button
                    onClick={() => onTabChange('reports')}
                    className={`${styles.navBtn} ${activeTab === 'reports' ? styles.active : ''}`}
                >
                    <span className={styles.navText}>Жалобы</span>
                    {activeTab === 'reports' && <span className={styles.activeIndicator} />}
                </button>
            </nav>
        </div>
    );
}

export default ModeratorSidebar;