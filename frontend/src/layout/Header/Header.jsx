import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import styles from './Header.module.css';
import { userApi } from '../../api/userApi';
import PFP from '../../assets/WA.jpg';

function Header() {
    const { user, isAuthenticated, logout, isLoading, isProcessing, isAuthChecked } = useAuth();
    const location = useLocation();

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