import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import styles from './Header.module.css';
import { userApi } from '../../api/userApi';
import PFP from '../../assets/WA.jpg';

function Header() {
    const { user, isAuthenticated, logout, isLoading, isProcessing } = useAuth();
    const location = useLocation();

    const userRole = user?.userRole || null;
    const isAdmin = userRole === 'ADMIN';
    const isModerator = userRole === 'MODERATOR';
    
    const isAdminPage = location.pathname.startsWith('/admin');
    const isModeratorPage = location.pathname.startsWith('/moderator');
    
    const showAdminButton = isAdmin && !isAdminPage && !isModeratorPage;
    const showModeratorButton = isModerator && !isAdmin && !isAdminPage && !isModeratorPage;

    const getAvatarUrl = () => {
        if (!user) return PFP;
        return userApi.getFullUrl(user.avatarUrl) || PFP;
    };

    const handleLogout = async () => {
        try {
            await logout();
        } catch (error) {
            console.error('Logout error:', error);
        }
    };

    if (isLoading) {
        return (
            <div className={styles.header}>
                <Link to="/" className={styles.link}>
                    <span className={styles.text}>ARTSHIP</span>
                </Link>
                <div className={styles.container}>
                    <div className={styles.loading}>...</div>
                </div>
            </div>
        );
    }

    return (
        <div className={styles.header}>
            <Link to="/" className={styles.link}>
                <span className={styles.text}>ARTSHIP</span>
            </Link>

            <div className={styles.container}>
                {!isAuthenticated ? (
                    <>
                        <Link to="/login" className={styles.cover}>Войти</Link>
                        <Link to="/register" className={styles.cover}>Зарегистрироваться</Link>
                    </>
                ) : (
                    <>
                        {showAdminButton && (
                            <Link to="/admin" className={styles.cover}>
                                Панель администратора
                            </Link>
                        )}
                        
                        {showModeratorButton && (
                            <Link to="/moderator" className={styles.cover}>
                                Панель модератора
                            </Link>
                        )}
                        
                        {location.pathname === '/me' ? (
                            <button 
                                onClick={handleLogout} 
                                className={styles.cover}
                                disabled={isProcessing}
                            >
                                {isProcessing ? 'Выход...' : 'Выйти'}
                            </button>
                        ) : (
                            <Link to="/me" className={styles.avatarLink}>
                                <img 
                                    className={styles.avatar}
                                    src={getAvatarUrl()}
                                    alt="Аватарка" 
                                    onError={(e) => {
                                        e.target.onerror = null;
                                        e.target.src = PFP;
                                    }}
                                />
                            </Link>
                        )}
                    </>
                )}
            </div>
        </div>
    );
}

export default Header;